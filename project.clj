(defproject fatty "0.1.0"
  :description "Builds 'fat' binaries, with all their dependencies, into tarballs and self-installing shell scripts."
  :main fatty.core
  :shell-wrapper true 
  :dev-dependencies [[vimclojure/server "2.2.0-SNAPSHOT"]]
  :dependencies [[org.clojure/clojure "1.2.0"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [log4j "1.2.15" :exclusions [javax.mail/mail
                                              javax.jms/jms
                                              com.sun.jdmk/jmxtools
                                              com.sun.jmx/jmxri]]])
