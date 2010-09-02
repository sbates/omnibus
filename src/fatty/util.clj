(ns fatty.util
  (:use [fatty.log]
        [clojure.java.shell :only [sh]]
        [clojure.contrib.io :only [make-parents file-str]] )
  (:require [clojure.contrib.string :as str])
  (:gen-class))
  
(defn- copy-source-to-build
  "Copy the source directory to the build directory"
  [build-root source-root soft]
  (let [status (sh "cp" "-r" (.getPath (file-str source-root "/" (soft :source))) (.getPath build-root))]
    (log-sh-result status 
                   (str "Copied " (soft :source) " to build directory.")
                   (str "Failed to copy " (soft :source) " to build directory."))))

(defn clean
  "Clean a previous build directory"
  [build-root soft]
  (when-let [src (soft :source)]
    (let [status (sh "rm" "-rf" (.getPath (file-str build-root "/" src)) )]
      (log-sh-result status
                     (str "Removed old build directory for " src)
                     (str "Failed to remove old build directory for " src)))))

(defn prep
  "Prepare to build a software package by copying its source to a pristine build directory"
  [build-root source-root soft]
  (when (soft :source)
    (do
      (.mkdirs build-root)
      (copy-source-to-build build-root source-root soft))))
