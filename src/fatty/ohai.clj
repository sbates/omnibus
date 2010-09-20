(ns fatty.ohai
  (:use [fatty.log]
        [clojure.contrib.json]
        [clojure.contrib.logging :only [log]]
        [clojure.contrib.io :only [make-parents file-str]]
        [clojure.java.shell :only [sh]])
  (:require [clojure.contrib.string :as str])
  (:gen-class))

(defn ohai
  "Use Ohai to get our Operating System and Machine Architecture"
  []
  (println "calling os-and-machine")
  (let [ohai-data (read-json ((sh "ohai") :out))]
    {:os (get ohai-data :os), :machine (get-in ohai-data [:kernel :machine])}))

(def ohai (memoize ohai))

(defn os-and-machine
  [& ohai-keys]
  (get-in (ohai) ohai-keys))

(defn is-os?
  "Returns true if the current OS matches the argument"
  [to-check]
  (= (os-and-machine :os) to-check))

(defn is-machine?
  "Returns true if the current machine matches the argument"
  [to-check]
  (= (os-and-machine :machine) to-check))

