(defproject fatty "1.0.0-SNAPSHOT"
  :description "FIXME: write"
  :dev-dependencies [[vimclojure/server "2.2.0-SNAPSHOT"]]
  :dependencies [[org.clojure/clojure "1.2.0-RC3"]
                 [org.clojure/clojure-contrib "1.2.0-RC3"]
                 [log4j "1.2.15" :exclusions [javax.mail/mail
                                              javax.jms/jms
                                              com.sun.jdmk/jmxtools
                                              com.sun.jmx/jmxri]]])
