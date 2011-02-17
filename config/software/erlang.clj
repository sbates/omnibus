(software "erlang" :source "otp_src_R14B"
          :steps [
                  [ "./configure" "--prefix=/opt/opscode/embedded" "--enable-threads" "--enable-smp-support" "--enable-kernel-poll" "--enable-hipe" "--enable-shared-zlib" "--without-javac" "--with-ssl=/opt/opscode/embedded" "--disable-debug" "CFLAGS=-L/opt/opscode/embedded/lib -I/opt/opscode/embedded/include" ]
                  [ "touch" "lib/wx/SKIP" ]
                  ["make"]
                  ["make" "install"]
                  ])
