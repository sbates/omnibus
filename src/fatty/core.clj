(ns fatty.core
  (:use [clojure.contrib.shell-out :only [sh]]
        [clojure.contrib.logging :only [log]]
        [clojure.contrib.json :onle [read-json]]
        [clojure.contrib.io :only [make-parents file-str]])
  (:require [clojure.contrib.string :as str]))

(def fatty-home-dir (. System getProperty "user.dir"))
(def fatty-source-dir (file-str fatty-home-dir "/source"))
(def fatty-build-dir (file-str fatty-home-dir "/build"))
(def fatty-pkg-dir (file-str fatty-home-dir "/pkg"))

(def software-map (ref {}))

(defstruct software-desc :name :source :build_subdir :steps)
 
(defn software
  "Create a new software description"
  [software-name & instruction-map]
  (let [new-map (conj instruction-map software-name :name)]
    (dosync (ref-set software-map (assoc @software-map software-name (apply struct-map software-desc new-map))))))

(software "zlib"  :source "zlib-1.2.5" :steps [["./configure" "--prefix=/opt/opscode/embedded"]
                                               ["make"]
                                               ["sudo" "make" "install"]])

(software "libiconv" :source "libiconv-1.13.1"
                     :steps [["./configure" "--prefix=/opt/opscode/embedded"]
                             ["make"]
                             ["sudo" "make" "install"]])

(software "db" :source "db-5.0.26.NC"
               :build-subdir "build_unix"
               :steps [["../dist/configure" "--prefix=/opt/opscode/embedded"]
                       ["make"]
                       ["sudo" "make" "install"]])

(software "gdbm" :source "gdbm-1.8.3"
                 :steps [["./configure" "--prefix=/opt/opscode/embedded"]
                         ["make"]
                         ["sudo" "make" "install"]])

(software "ncurses" :source "ncurses-5.7"
                    :steps [["./configure" "--prefix=/opt/opscode/embedded" "--with-shared" "--with-normal" "--without-debug"]
                            ["make"]
                            ["sudo" "make" "install"]])

(software "ncurses" :source "readline-5.2"
                     :steps [["./configure" "--prefix=/opt/opscode/embedded"]
                             ["make"]
                             ["sudo" "make" "install"]])

(software "openssl" :source "openssl-0.9.8o"
                    :steps [["./config" "--prefix=/opt/opscode/embedded" "--with-zlib-lib=/opt/opscode/embedded/lib" "--with-zlib-include=/opt/opscode/embedded/include" "zlib" "shared"]
                            ["bash" "-c" "make"]
                            ["sudo" "make" "install"]])

(software "libxml2" :source "libxml2-2.7.7"
                    :steps [["./configure" "--prefix=/opt/opscode/embedded" "--with-zlib=/opt/opscode/embedded" "--with-readline=/opt/opscode/embedded" "--with-iconv=/opt/opscode/embedded"]
                            ["make"]
                            ["sudo" "make" "install"]])

(software "libxslt" :source "libxslt-1.1.26"
                    :steps [["./configure" "--prefix=/opt/opscode/embedded" "--with-libxml-prefix=/opt/opscode/embedded" "--with-libxml-include-prefix=/opt/opscode/embedded/include" "--with-libxml-libs-prefix=/opt/opscode/embedded/lib"]
                            ["make"]
                            ["sudo" "make" "install"]])

(software "ruby" :source "ruby-1.9.2-p0"
                 :steps [["./configure" "--prefix=/opt/opscode/embedded" "--with-opt-dir=/opt/opscode/embedded" "--enable-shared" "--disable-install-doc"]
                         ["make"]
                         ["sudo" "make" "install"]])

(software "chef" :source "chef"
                 :steps [["sudo" "/opt/opscode/embedded/bin/gem" "install" "chef" "fog" "highline" "net-ssh-multi" "-n" "/opt/opscode/bin"]])

(def project-map (ref {}))

(defn project
  "Create a new project"
  [project-name build-order]
  (dosync (ref-set project-map (assoc @project-map project-name build-order))))

(project "chef-full" [ "zlib" "libiconv" "db" "gdbm" "ncurses" "openssl" "libxml2" "libxslt" "ruby" "chef" ])

(defn- log-sh-result
  [status true-log false-log]
  (if (= (status :exit) 0)
    (do
      (log :info true-log)
      true)
    (do
      (log :error false-log)
      (log :error (str "STDOUT: " (status :out)))
      (log :error (str "STDERR: " (status :err)))
      (System/exit 2))))

(defn- copy-source-to-build
  "Copy the source directory to the build directory"
  [soft]
  (let [status (sh "cp" "-r" (.getPath (file-str fatty-source-dir "/" (soft :source))) (.getPath fatty-build-dir) :return-map true)]
    (log-sh-result status 
                   (str "Copied " (soft :source) " to build directory.")
                   (str "Failed to copy " (soft :source) " to build directory."))))

(defn clean
  "Clean a previous build directory"
  [soft]
  (if (not (= (soft :source) nil))
    (let [status (sh "rm" "-rf" (.getPath (file-str fatty-build-dir "/" (soft :source))) :return-map true)]
      (log-sh-result status
                     (str "Removed old build directory for " (soft :source))
                     (str "Failed to remove old build directory for " (soft :source))))))

(defn prep
  "Prepare to build a software package by copying its source to a pristine build directory"
  [soft]
  (if (not (= (soft :source) nil))
    (do
      (.mkdirs fatty-build-dir)
      (copy-source-to-build soft))))

(defn execute-step
  "Run a build step"
  [step path]
  (let [status (apply sh (flatten [step :return-map true :dir path]))]
    (log-sh-result status
                   (str "Build Command succeeded: " step)
                   (str "Build Command failed: " step))))

(defn run-steps
  "Run the steps for a given piece of software"
  [soft]
  (log :info (str "Building " (soft :source)))
  (dorun (for [step (soft :steps)] 
    (execute-step step (.getPath (if (= (soft :source) nil)
                                   (file-str fatty-build-dir)
                                   (if (= (soft :build-subdir) nil)
                                     (file-str fatty-build-dir "/" (soft :source))
                                     (file-str fatty-build-dir "/" (soft :source) "/" (soft :build-subdir)))))))))

(defn build 
  "Build a software package - runs prep for you"
  [soft]
  (do
    (clean soft)
    (prep soft)
    (run-steps soft)))

(defn get-os-and-machine
  "Use Ohai to get our Operating System and Machine Architecture"
  []
  (let [ohai-data (read-json (sh "ohai"))]
    {:os (get ohai-data :os), :machine (get-in ohai-data [:kernel :machine])}))

(defn build-fat-binary
  "Build a fat binary"
  [project-name]
  (dorun (for [software-pkg (project-map project-name)] (build (software-map software-pkg))))
  (let [os-data (get-os-and-machine)]
    (do
      (let [status (sh "tar" "czf" (file-str fatty-pkg-dir "/" project-name "-" (os-data :os) "-" (os-data :machine) ".tar.gz") (file-str "/opt/opscode"))]
        (log-sh-result status
                       (str "Created package for " project-name " on " (os-data :os) " machine arch " (os-data :machine))
                       (str "Failed to create package for " project-name " on " (os-data :os) " machine arch " (os-data :machine)))))))

