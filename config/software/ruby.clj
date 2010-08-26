(software "ruby" :source "ruby-1.9.2-p0"
                 :steps [["./configure" "--prefix=/opt/opscode/embedded" "--with-opt-dir=/opt/opscode/embedded" "--enable-shared" "--disable-install-doc"]
                         ["make"]
                         ["sudo" "make" "install"]])
