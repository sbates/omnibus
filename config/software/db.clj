(software "db" :source "db-5.0.26.NC"
               :build-subdir "build_unix"
               :steps [["env" "LDFLAGS=-R/opt/opscode/embedded/lib -L/opt/opscode/embedded/lib -I/opt/opscode/embedded/include" "CFLAGS=-L/opt/opscode/embedded/lib -I/opt/opscode/embedded/lib" "../dist/configure" "--prefix=/opt/opscode/embedded"]
                       ["make"]
                       ["make" "install"]])
