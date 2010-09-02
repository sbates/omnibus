(ns fatty.steps
  (:use [fatty.log]
        [clojure.contrib.logging :only [log]]
        [clojure.contrib.io :only [make-parents file-str]]
        [clojure.java.shell :only [sh]])
  (:require [clojure.contrib.string :as str])
  (:gen-class))

(defn- execute-step
  "Run a build step"
  [step path]
  (when step 
    (let [status (apply sh (flatten [step :dir path]))]
      (log-sh-result status
                     (str "Build Command succeeded: " step)
                     (str "Build Command failed: " step)))))
(defn run-steps
  "Run the steps for a given piece of software"
  [build-root soft]
  (log :info (str "Building " (soft :source)))
  (dorun (for [step (soft :steps)] 
    (execute-step step (.getPath (if (= (soft :source) nil)
                                   (file-str build-root)
                                   (if (= (soft :build-subdir) nil)
                                     (file-str build-root "/" (soft :source))
                                     (file-str build-root "/" (soft :source) "/" (soft :build-subdir)))))))))
