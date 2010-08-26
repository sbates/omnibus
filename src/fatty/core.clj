(ns fatty.core
  (:use [clojure.java.shell :only [sh]]
        [clojure.contrib.logging :only [log]]
        [clojure.contrib.json]
        [clojure.contrib.io :only [make-parents file-str]])
  (:require [clojure.contrib.string :as str]))

(def fatty-home-dir (. System getProperty "user.dir"))
(def fatty-source-dir (file-str fatty-home-dir "/source"))
(def fatty-build-dir (file-str fatty-home-dir "/build"))
(def fatty-pkg-dir (file-str fatty-home-dir "/pkg"))
(def fatty-makeself-dir (file-str fatty-home-dir "/makeself"))

(def software-map (ref {}))

(defstruct software-desc :name :source :build_subdir :steps)
 
(defn software
  "Create a new software description"
  [software-name & instruction-map]
  (let [new-map (conj instruction-map software-name :name)]
    (dosync (ref-set software-map (assoc @software-map software-name (apply struct-map software-desc new-map))))))

(def project-map (ref {}))

(defn project
  "Create a new project"
  [project-name version build-order]
  (dosync (ref-set project-map (assoc @project-map project-name { :version version :build-order build-order }))))

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
  (let [status (sh "cp" "-r" (.getPath (file-str fatty-source-dir "/" (soft :source))) (.getPath fatty-build-dir))]
    (log-sh-result status 
                   (str "Copied " (soft :source) " to build directory.")
                   (str "Failed to copy " (soft :source) " to build directory."))))

(defn clean
  "Clean a previous build directory"
  [soft]
  (if (not (= (soft :source) nil))
    (let [status (sh "rm" "-rf" (.getPath (file-str fatty-build-dir "/" (soft :source))) )]
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
  (if (not (= step nil))
    (let [status (apply sh (flatten [step :dir path]))]
      (log-sh-result status
                     (str "Build Command succeeded: " step)
                     (str "Build Command failed: " step)))))

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
  (let [ohai-data (read-json ((sh "ohai") :out))]
    {:os (get ohai-data :os), :machine (get-in ohai-data [:kernel :machine])}))

(def os-and-machine (ref (get-os-and-machine)))

(defn build-tarball
  [project-name version os-data]
  (let [status (sh "tar" "czf" (.toString (file-str fatty-pkg-dir "/" project-name "-" version "-" (os-data :os) "-" (os-data :machine) ".tar.gz")) "opscode" :dir "/opt")]
    (log-sh-result status
                   (str "Created tarball package for " project-name " on " (os-data :os) " " (os-data :machine))
                   (str "Failed to create tarball package for " project-name " on " (os-data :os) " " (os-data :machine)))))

(defn build-makeself
  [project-name version os-data]
  (let [status (sh (.toString (file-str fatty-makeself-dir "/makeself.sh")) 
                   "--gzip" 
                   "/opt/opscode" 
                   (.toString (file-str fatty-pkg-dir "/" project-name "-" version "-" (os-data :os) "-" (os-data :machine) ".sh"))
                   (str "'Opscode " project-name " " version "'")
                   "./setup.rb"
                   :dir fatty-home-dir)]
    (log-sh-result status
                   (str "Created shell archive for " project-name " on " (os-data :os) " " (os-data :machine))
                   (str "Failed to create shell archive for " project-name " on " (os-data :os) " " (os-data :machine)))))

(defn build-fat-binary
  "Build a fat binary"
  [project-name]
  (dorun (for [software-pkg (get-in @project-map [project-name :build-order])] (build (software-map software-pkg))))
  (let [project-version (get-in @project-map [project-name :version])]
    (do
      (build-tarball project-name project-version os-and-machine)
      (build-makeself project-name project-version os-and-machine))))

(defn load-configuration
  "Load the fatty configuration files"
  []
  (dorun (for [file-obj (.listFiles (file-str fatty-home-dir "/config/projects"))] (load-file (.toString file-obj))))
  (dorun (for [file-obj (.listFiles (file-str fatty-home-dir "/config/software"))] (load-file (.toString file-obj)))))

(defn is-os?
  "Returns true if the current OS matches the argument"
  [to-check]
  (= (os-and-machine :os) to-check))

(defn is-machine?
  "Returns true if the current machine matches the argument"
  [to-check]
  (= (os-and-machine :machine) to-check))

(load-configuration)
