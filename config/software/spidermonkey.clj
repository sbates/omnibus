(software "spidermonkey" :source "js"
          :build-subdir "src"
          :steps [["make" "BUILD_OPT=1" "XCFLAGS=-L/opt/opscode/embedded/lib -I/opt/opscode/embedded/include" "-f" "Makefile.ref"]
                  ["make" "BUILD_OPT=1" "JS_DIST=/opt/opscode/embedded" "-f" "Makefile.ref" "export"]
                  ["mv" "/opt/opscode/embedded/lib64/libjs.a" "/opt/opscode/embedded/lib"]
                  ["mv" "/opt/opscode/embedded/lib64/libjs.so" "/opt/opscode/embedded/lib"]
                  ["rm" "-rf" "/opt/opscode/embedded/lib64"]
                  ])


