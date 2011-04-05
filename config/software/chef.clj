(software "chef" :source "chef"
                 :steps [["/opt/opscode/embedded/bin/gem" "install" "chef" "fog" "highline" "net-ssh-multi" "-n" "/opt/opscode/bin" "--no-rdoc" "--no-ri"]
                         ["cp" "setup.sh" "/opt/opscode"]
                         ["chmod" "755" "/opt/opscode/setup.sh"]
                         ["rm" "-rf" "/opt/opscode/embedded/docs"]
                         ["rm" "-rf" "/opt/opscode/embedded/share/doc"]
                         ["rm" "-rf" "/opt/opscode/embedded/share/gtk-doc"]
                         ["rm" "-rf" "/opt/opscode/embedded/ssl/man"]
                         ["rm" "-rf" "/opt/opscode/embedded/man"]
                         ["rm" "-rf" "/opt/opscode/embedded/info"]])

