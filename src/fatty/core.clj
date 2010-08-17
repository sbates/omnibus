(ns fatty.core)

(defstruct software-desc :source :build_subdir :steps)

(defn software
  "Create a new software description"
  [& map]
  (apply struct-map software-desc map))

(def zlib (software :source "zlib-1.2.5.tar.gz"
                    :steps ["./configure --prefix=/opt/opscode/embedded"
                            "make"
                            "make install"])

(def db (software :source "db-5.0.26.NC.tar.gz"
          :build_subdir "build-unix"
          :steps ["./configure --prefix=/opt/opscode/embedded"
                  "make"
                  "make install"])

(def gdbm (software :source "gdbm-1.8.3.tar.gz"
          :steps ["./configure --prefix=/opt/opscode/embedded"
                  "make"
                  "make install"])

