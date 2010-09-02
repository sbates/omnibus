(ns fatty.log
  (:use [clojure.contrib.logging :only [log]])
  (:require [clojure.contrib.string :as str])
  (:gen-class))

(defn log-sh-result
  [status true-log false-log]
  (if (zero? (status :exit))
    (do
      (log :info true-log)
      true)
    (do
      (log :error false-log)
      (log :error (str "STDOUT: " (status :out)))
      (log :error (str "STDERR: " (status :err)))
      (System/exit 2))))
