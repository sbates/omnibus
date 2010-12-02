(software "icu" :source "icu"
          :build-subdir "source"
          :steps [
                  ["./configure" "--prefix=/opt/opscode/embedded" "CFLAGS=-L/opt/opscode/embedded/lib -I/opt/opscode/embedded/include" ]
                  ["make"]
                  ["make" "install"]
                  ])

