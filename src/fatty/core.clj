;;
;; Author:: Adam Jacob (<adam@opscode.com>)
;; Author:: Christopher Brown (<cb@opscode.com>)
;; Copyright:: Copyright (c) 2010 Opscode, Inc.
;; License:: Apache License, Version 2.0
;;
;; Licensed under the Apache License, Version 2.0 (the "License");
;; you may not use this file except in compliance with the License.
;; You may obtain a copy of the License at
;; 
;;     http://www.apache.org/licenses/LICENSE-2.0
;; 
;; Unless required by applicable law or agreed to in writing, software
;; distributed under the License is distributed on an "AS IS" BASIS,
;; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
;; See the License for the specific language governing permissions and
;; limitations under the License.
;;

(ns fatty.core
  (:use [fatty.ohai]
        [fatty.steps]
        [fatty.log]
        [fatty.util]
        [clojure.java.shell :only [sh]]
        [clojure.contrib.logging :only [log]]        
        [clojure.contrib.json]
        [clojure.contrib.command-line]
        [clojure.contrib.io :only [make-parents file-str]])
  (:require [clojure.contrib.string :as str])
  (:gen-class))

(def *fatty-home-dir* (. System getProperty "user.dir"))
(def *fatty-source-dir* (file-str *fatty-home-dir* "/source"))
(def *fatty-software-dir* (file-str *fatty-home-dir* "/config/software"))
(def *fatty-projects-dir* (file-str *fatty-home-dir* "/config/projects"))
(def *fatty-build-dir* (file-str *fatty-home-dir* "/build"))
(def *fatty-pkg-dir* (file-str *fatty-home-dir* "/pkg"))
(def *fatty-makeself-dir* (file-str *fatty-home-dir* "/makeself"))

(defstruct software-desc
  :name
  :source
  :build_subdir
  :steps)

(defstruct build-desc
  :name
  :version
  :build-order)
 
(defn software
  "Create a new software description"
  [software-name & instruction-map]
  (do
    (apply struct-map software-desc (conj instruction-map software-name :name ))))

(defn project
  "Create a new project"
  [project-name version & build-vals]
  (apply struct-map build-desc (conj build-vals project-name :name version :version)))

(defn- load-forms
  "Load DSL configurations from specified directory"
  [directory]
  (for [file-name (map file-str (.listFiles directory))]
    (with-in-str (slurp file-name)
      (let [eval-form (read)]
        (eval `(do (use 'fatty.core 'fatty.ohai)  ~eval-form))))))

;; NOTE: we could simply load-file above, but at some point we'll want multiple forms
;; in a file and we'll want to iterate over them using load-reader & eval to collect their output

(defn build-software
  "Build a software package - runs prep for you"
  [soft]
  (do
    (clean *fatty-build-dir* soft)
    (prep *fatty-build-dir* *fatty-source-dir* soft)
    (run-steps *fatty-build-dir* soft)))

(defn build-software-by-name
  "Build a software package by name, rather than by clojure form"
  [software-name]
  (log :info (str "Building " software-name))
  (let [mapper #(assoc %1 (%2 :name) %2)
        software-descs (reduce mapper  {}  (load-forms *fatty-software-dir*))]
    (build-software (software-descs software-name))))

(defn build-deb
  "Builds a deb"
  [project-name version os-data]
  (let [status (sh "./debian/build-deb" project-name version (os-data :machine))]
    (log-sh-result status
                   (str "Created debian package")
                   (str "Failed to create debian package"))))

(defn build-tarball
  "Builds a tarball of the entire mess"
  [project-name version os-data]
  (let [status (sh "tar" "czf" (.toString (file-str *fatty-pkg-dir* "/" project-name "-" version "-" (os-data :os) "-" (os-data :machine) ".tar.gz")) "opscode" :dir "/opt")]
    (log-sh-result status
                   (str "Created tarball package for " project-name " on " (os-data :os) " " (os-data :machine))
                   (str "Failed to create tarball package for " project-name " on " (os-data :os) " " (os-data :machine)))))

(defn build-makeself
  [project-name version os-data]
  (let [status (sh (.toString (file-str *fatty-makeself-dir* "/makeself.sh")) 
                   "--gzip" 
                   "/opt/opscode" 
                   (.toString (file-str *fatty-pkg-dir* "/" project-name "-" version "-" (os-data :os) "-" (os-data :machine) ".sh"))
                   (str "'Opscode " project-name " " version "'")
                   "./setup.sh"
                   :dir *fatty-home-dir*)]
    (log-sh-result status
                   (str "Created shell archive for " project-name " on " (os-data :os) " " (os-data :machine))
                   (str "Failed to create shell archive for " project-name " on " (os-data :os) " " (os-data :machine)))))

(defn build-project
  "Build a project by building all the software in the appropriate build order"
  [project software-descs]
  (do
    (println (str "\n-------------------------\nBuilding project '" (project :name) "'..."))
    (let [build-order (project :build-order)]
      (dorun (for [soft build-order]
               (build-software (software-descs soft)))))
    (println "build complete...\n------------------------")))

(defn build-fat-binary
  "Build a fat binary"
  [project-name]
  (let [mapper #(assoc %1 (%2 :name) %2)
        software-descs (reduce mapper  {}  (load-forms *fatty-software-dir*))
        projects  (reduce mapper {} (load-forms *fatty-projects-dir*))]
    (do
      (try
        (build-project (projects project-name) software-descs)
        (catch NullPointerException e
          (do
            (println (str "Can't find project '" project-name "'!"))
            (System/exit -2))))
     (build-tarball project-name ((projects project-name) :version) os-and-machine)
     (build-makeself project-name ((projects project-name) :version) os-and-machine)
     (build-deb project-name ((projects project-name) :version) os-and-machine))))

(defn -main
  "Main entry point when run from command line"
  [& args]
  (with-command-line args
    "Specify the project you'd like me to build..."
    [[project-name "The name of the project to build"]]
    (build-fat-binary project-name)))

