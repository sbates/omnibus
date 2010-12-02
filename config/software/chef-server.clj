(software "chef-server" :source "chef"
                 :steps [[ "/opt/opscode/embedded/bin/gem" "install" "chef-server" "-n" "/opt/opscode/bin" "--no-rdoc" "--no-ri"]
                         [ "chown" "-R" (cond (is-os? "darwin") "root:wheel" true "root:root") "/opt/opscode"]])


