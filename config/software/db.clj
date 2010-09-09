(software "db" :source "db-5.0.26.NC"
               :build-subdir "build_unix"
               :steps [["../dist/configure" "--prefix=/opt/opscode/embedded"]
                       ["make"]
                       [ "make" "install"]])
