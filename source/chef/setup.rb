#!./embedded/bin/ruby
#
# Install a full Opscode Client
#

chef_url = ARGV[0] 
validation_client_name = nil
unless chef_url
  puts "The first argument to the installer must be an organization or URL"
  puts "Example:"
  puts "  'opscode' or 'http://chef.example.com' "
  exit 2
end
if chef_url !~ /^http(s?):\/\//
  validation_client_name = "#{chef_url}-validator"
  chef_url = "https://api.opscode.com/organizations/#{chef_url}"
end

path_to_validation_key = ARGV[1]
unless path_to_validation_key
  puts "The second argument ot the installer must be the path to your validation key."
  puts "Example:"
  puts "  '~/validation.pem'"
  exit 3
end
unless File.exists?(path_to_validation_key)
  puts "#{path_to_validation_key} does not exist!"
  puts "Please provide a path to an existing validation key."
  exit 4
end

if Process.uid != 0
  puts "This installer must be run as root."
  exit 1
end

def run_command(cmd)
  s = system(cmd)
  unless s
    puts "#{cmd} failed!"
    puts "Installer aborting!"
    exit 1
  end
end

installer_dir = File.expand_path(File.dirname(__FILE__))

run_command("mkdir -p /opt/opscode")
run_command("#{installer_dir}/embedded/bin/rsync -a --delete --exclude #{installer_dir}/setup.rb #{installer_dir}/ /opt/opscode")
run_command("mkdir -p /etc/chef")
run_command("cp #{path_to_validation_key} /etc/chef/validation.pem")
run_command("chmod 600 /etc/chef/validation.pem")
File.open("/etc/chef/client.rb", "w") do |crb|
  crb.puts <<EOH
log_level                :info
log_location             STDOUT
chef_server_url          "#{chef_url}"
EOH
  if validation_client_name
    crb.puts "validation_client_name   '#{validation_client_name}'"
  end
end

puts <<EOH
Thanks for installing Chef!

You can now run /opt/opscode/bin/chef-client to configure your system.
EOH
exit 0

