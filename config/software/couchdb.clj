(software "couchdb" :source "apache-couchdb-1.0.1"
          :steps [
                  ["./configure" "--prefix=/opt/opscode/embedded" "--disable-init" "--disable-launchd" "--with-erlang=/opt/opscode/embedded" "--with-js-include=/opt/opscode/embedded/include" "--with-js-lib=/opt/opscode/embedded/lib" "CFLAGS=-L/opt/opscode/embedded/lib -I/opt/opscode/embedded/include"]
                  ["env RPATH=/opt/opscode/embedded/lib make"]
                  ["make" "install"]
                  ])

