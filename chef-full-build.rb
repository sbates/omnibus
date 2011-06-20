#!/usr/bin/env ruby

require 'chef'
require 'chef/knife'
require 'chef/knife/ec2_server_create'
require 'chef/knife/ec2_server_delete'
require 'chef/knife/rackspace_server_create'
require 'chef/knife/ssh'
require 'chef/knife/bootstrap'

BASE_PATH = File.dirname(__FILE__)
IO.read("./config/projects/chef-full.clj") =~ /project "chef-full" "(.+)" "(.+)"/
BUILD_VERSION = "#{$1}-#{$2}"

Chef::Config.from_file("#{BASE_PATH}/chef-repo/.chef/knife.rb")

hosts_to_build = {
  'ubuntu-10.04-i686' => {
    "provider" => "ec2",
    "image" => "ami-7000f019",
    "flavor" => "m1.small",
    "ssh_user" => "ubuntu",
    "distro" => "ubuntu10.04-gems",
    "ssh_key_name" => "fatty"
  },
  'ubuntu-10.04-x86_64' => {
    "provider" => "ec2",
    "image" => "ami-fa01f193",
    "flavor" => "m1.large",
    "ssh_user" => "ubuntu",
    "distro" => "ubuntu10.04-gems",
    "ssh_key_name" => "fatty"
  },
    'ubuntu-10.10-i686' => {
    "provider" => "ec2",
    "image" => "ami-a6f504cf",
    "flavor" => "m1.small",
    "ssh_user" => "ubuntu",
    "distro" => "ubuntu10.04-gems",
    "ssh_key_name" => "fatty"
  },
  'ubuntu-10.10-x86_64' => {
    "provider" => "ec2",
    "image" => "ami-08f40561",
    "flavor" => "m1.large",
    "distro" => "ubuntu10.04-gems",
    "ssh_user" => "ubuntu",
    "ssh_key_name" => "fatty"
  },
  'ubuntu-11.04-i686' => {
    "provider" => "ec2",
    "image" => "ami-e2af508b",
    "flavor" => "m1.small",
    "distro" => "ubuntu10.04-gems",
    "ssh_user" => "ubuntu",
    "ssh_key_name" => "fatty"
  },
  'ubuntu-11.04-x86_64' => {
    "provider" => "ec2",
    "image" => "ami-68ad5201",
    "flavor" => "m1.large",
    "distro" => "ubuntu10.04-gems",
    "ssh_user" => "ubuntu",
    "ssh_key_name" => "fatty"
  },
  'debian-5.0-i686' => {
    "provider" => "ec2",
    "image" => "ami-dcf615b5",
    "flavor" => "m1.small",
    "distro" => "ubuntu10.04-gems",
    "ssh_user" => "root",
    "ssh_key_name" => "fatty"
  },
  'debian-5.0-x86_64' => {
    "provider" => "ec2",
    "image" => "ami-f0f61599",
    "flavor" => "m1.large",
    "distro" => "ubuntu10.04-gems",
    "ssh_user" => "root",
    "ssh_key_name" => "fatty"
  },
  'redhat-5.6-i686' => {
    "provider" => "ec2",
    "image" => "ami-4828d121",
    "flavor" => "m1.small",
    "distro" => "centos5-gems",
    "ssh_user" => "root",
    "ssh_key_name" => "fatty"
  },
}

def terminate_instance(host_type)
  output_file = "#{BASE_PATH}/build-output/#{host_type}.out"
  `head -1 #{output_file}`.chomp! =~ /Instance ID: (.+)/
  instance_id = $1
  Chef::Knife::Ec2ServerDelete.load_deps
  r = Chef::Knife::Ec2ServerDelete.new
  r.name_args = [ instance_id ]
  r.config[:yes] = true
  begin
    r.run
  rescue => e
    puts e 
  end
end

build_status = Hash.new
child_pids = Hash.new
build_at_a_time = 4 
total_hosts = hosts_to_build.keys.length
current_count = 0 
total_count = 0
hosts_to_build.each do |host_type, host_data|
  total_count += 1
  if ARGV.length > 0
    next unless ARGV.include?(host_type)
  end
  current_count += 1

  if (current_count == build_at_a_time) || total_hosts == total_count
    current_count = 0
    Process.waitall.each do |pstat|
      if pstat[1].exitstatus != 0
        build_status[host_type] = "failed"
        puts "Failed to build: #{child_pids[pstat[0]]}"
      else
        build_status[host_type] = "success"
      end
    end
  end
  pid = fork
  if pid
    child_pids[pid] = host_type
  else
    orig_stdout = $stdout
    $stdout = File.open("#{BASE_PATH}/build-output/#{host_type}.out", "w")
    orig_stdout.puts "Launching #{host_type}"
    if host_data["provider"] == "rackspace"
      Chef::Knife.load_deps
      Chef::Knife::Bootstrap.load_deps
      Chef::Knife::RackspaceServerCreate.load_deps
      r = Chef::Knife::RackspaceServerCreate.new
      Chef::Config[:knife][:flavor] = host_data["flavor"]
      Chef::Config[:knife][:image] = host_data["image"]
      Chef::Config[:knife][:distro] = host_data["distro"]
      r.config[:flavor] = host_data["flavor"]
      r.config[:image] = host_data["image"]
      r.config[:distro] = host_data["distro"]
      r.config[:ssh_user] = host_data["ssh_user"]
      r.config[:run_list] = ["role[fatty]"]
      exit_status = 0
      begin
        r.run 
      rescue => e
        exit_status = 1
      ensure
        terminate_instance(host_type)
      end
      if exit_status == 0
        connection = Fog::Storage.new(
          :provider => 'AWS',
          :aws_access_key_id => Chef::Config[:knife][:aws_access_key_id],
          :aws_secret_access_key => Chef::Config[:knife][:aws_secret_access_key]
        )
        directory = connection.directories.create(:key => "opscode-full-stack", :public => true)
        file = directory.files.create(:key => "#{host_type}/LATEST", :body => BUILD_VERSION, :public => true)
      end
      exit exit_status
    elsif host_data["provider"] = "ec2"
      Chef::Knife.load_deps
      Chef::Knife::Bootstrap.load_deps
      Chef::Knife::Ec2ServerCreate.load_deps
      r = Chef::Knife::Ec2ServerCreate.new
      Chef::Config[:knife][:flavor] = host_data["flavor"]
      Chef::Config[:knife][:image] = host_data["image"]
      Chef::Config[:knife][:distro] = host_data["distro"]
      Chef::Config[:knife][:aws_ssh_key_id] = host_data["ssh_key_name"]
      r.config[:security_groups] = [ "fatty" ]
      r.config[:flavor] = host_data["flavor"]
      r.config[:image] = host_data["image"]
      r.config[:distro] = host_data["distro"]
      r.config[:ssh_user] = host_data["ssh_user"]
      r.config[:aws_ssh_key_id] = host_data["ssh_key_name"]
      r.config[:run_list] = ["role[fatty]"]
      exit_status = 0
      begin
        r.run 
      rescue => e
        exit_status = 1
      ensure
        terminate_instance(host_type)
      end

      if exit_status == 0
        connection = Fog::Storage.new(
          :provider => 'AWS',
          :aws_access_key_id => Chef::Config[:knife][:aws_access_key_id],
          :aws_secret_access_key => Chef::Config[:knife][:aws_secret_access_key]
        )
        directory = connection.directories.create(:key => "opscode-full-stack", :public => true)
        file = directory.files.create(:key => "#{host_type}/LATEST", :body => BUILD_VERSION, :public => true)
      end
      exit exit_status
    end
  end
end

Process.waitall.each do |pstat|
  if pstat[1].exitstatus != 0
    build_status[child_pids[pstat[0]]] = "failed"
    puts "Failed to build: #{child_pids[pstat[0]]}"
  else
    build_status[child_pids[pstat[0]]] = "success"
  end
end

build_status.each do |key, value|
  puts "#{key}: #{value}"
end

