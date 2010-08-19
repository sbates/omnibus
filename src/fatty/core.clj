(ns fatty.core
  (:use [clojure.contrib.shell-out :only [sh]]
        [clojure.contrib.logging :only [log]]
        [clojure.contrib.io :only [make-parents file-str]])
  (:require [clojure.contrib.string :as str]))

(defstruct software-desc :source :build_subdir :steps)

(def fatty-home-dir (. System getProperty "user.dir"))
(def fatty-source-dir (file-str fatty-home-dir "/source"))
(def fatty-build-dir (file-str fatty-home-dir "/build"))
 
(defn software
  "Create a new software description"
  [& map]
  (apply struct-map software-desc map))
 
(def zlib (software :source "zlib-1.2.5"
                    :steps [["./configure" "--prefix=/opt/opscode/embedded"]
                            ["make"]
                            ["sudo" "make" "install"]]))
 
(def db (software :source "db-5.0.26.NC"
                  :build-subdir "build_unix"
                  :steps [["../dist/configure" "--prefix=/opt/opscode/embedded"]
                          ["make"]
                          ["sudo" "make" "install"]]))

(def gdbm (software :source "gdbm-1.8.3"
                    :steps [["./configure" "--prefix=/opt/opscode/embedded"]
                            ["make"]
                            ["sudo" "make" "install"]]))

(def ncurses (software :source "ncurses-5.7"
                       :steps [["./configure" "--prefix=/opt/opscode/embedded" "--with-shared" "--with-normal" "--without-debug"]
                               ["make"]
                               ["sudo" "make" "install"]]))

(def readline (software :source "readline-5.2"
                        :steps [["./configure" "--prefix=/opt/opscode/embedded"]
                                ["make"]
                                ["sudo" "make" "install"]]))

(def openssl (software :source "openssl-0.9.8o"
                       :steps [["./config" "--prefix=/opt/opscode/embedded" "--with-zlib-lib=/opt/opscode/embedded/lib" "--with-zlib-include=/opt/opscode/embedded/include" "zlib" "shared"]
                               ["bash" "-c" "make"]
                               ["sudo" "make" "install"]]))

(def ruby (software :source "ruby-1.9.2-p0"
                    :steps [["./configure" "--prefix=/opt/opscode/embedded" "--with-opt-dir=/opt/opscode/embedded" "--enable-shared" "--disable-install-doc"]
                            ["make"]
                            ["sudo" "make" "install"]]))

(def chef (software :steps [["/opt/embedded/bin/gems" "install" "chef" "-n" "/opt/opscode/bin"]]))

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
  (if (= (contains? soft :source))
    (let [status (sh "rm" "-rf" (.getPath (file-str fatty-build-dir "/" (soft :source))) :return-map true)]
      (log-sh-result status
                     (str "Removed old build directory for " (soft :source))
                     (str "Failed to remove old build directory for " (soft :source))))))

(defn prep
  "Prepare to build a software package by copying its source to a pristine build directory"
  [soft]
  (if (= (contains? soft :source))
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
  (for [step (soft :steps)] 
    (execute-step step (.getPath (file-str fatty-build-dir "/" 
                                           (if (= (contains? soft :build-subdir) true)
                                             (str (soft :source) "/" (soft :build-subdir))
                                             (if (= (contains? soft :source) true)
                                               (soft :source)
                                               ""))))))) ; It's cool, we just want the top build directory if there is no source

(defn build 
  "Build a software package - runs prep for you"
  [soft]
  (clean soft)
  (prep soft)
  (run-steps soft))

