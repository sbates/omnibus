(software "ree" :source "ruby-enterprise-1.8.7-2010.02"
          :steps [["bash" "-c" "cd ./source/distro/google-perftools-1.4 && ./configure --prefix=/opt/opscode/embedded --disable-dependency-tracking && make libtcmalloc_minimal.la"]
                  ["sudo" "mkdir" "-p" "/opt/opscode/embedded/lib"]
                  ["sudo" "bash" "-c" (str "cp -Rpf " (cond (is-os? "darwin") "./source/distro/google-perftools-1.4/.libs/libtcmalloc_minimal.*" 
                                                            (is-os? "linux") "./source/distro/google-perftools-1.4/.libs/libtcmalloc_minimal.*") " /opt/opscode/embedded/lib")]
                  ["bash" "-c" "cd ./source && ./configure --prefix=/opt/opscode/embedded --enable-mbari-api CFLAGS='-g -O2' --with-opt-dir=/opt/opscode/embedded"]
                  ["bash" "-c" 
                   (cond (is-os? "darwin") "cd ./source && make PRELIBS=\"-Wl,-rpath,/opt/opscode/embedded/lib -L/opt/opscode/embedded/lib -lsystem_allocator -ltcmalloc_minimal\""
                         (is-os? "linux") "cd ./source && make PRELIBS=\"-Wl,-rpath,/opt/opscode/embedded/lib -L/opt/opscode/embedded/lib -ltcmalloc_minimal\"")]
                  ["sudo" "bash" "-c" "cd ./source && make install"]])
