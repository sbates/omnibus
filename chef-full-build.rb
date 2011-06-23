#!/usr/bin/env ruby

require 'systemu'

BASE_PATH = File.dirname(__FILE__)
VM_BASE_PATH = File.expand_path("~/Documents/Virtual Machines.localized")
PROJECT = ARGV[0]
BUCKET = ARGV[1]
S3_ACCESS_KEY = ARGV[2]
S3_SECRET_KEY = ARGV[3]

hosts_to_build = {
  'debian-6-i686' => {
    "vm" => "debian-6-i686.vmwarevm"
  },
  'debian-6-x86_64' => {
    "vm" => "debian-6-x86_86.vmwarevm"
  },
  'el-6-i686' => {
    "vm" => "SL-6-i386.vmwarevm"
  },
  'el-6-x86_64' => {
    "vm" => "SL-6-x86_86.vmwarevm"
  },
}

def run_command(cmd)
  status, stdout, stderr = systemu cmd 
  raise "Command failed: #{stdout}, #{stderr}" if status.exitstatus != 0
end

build_status = Hash.new
child_pids = Hash.new
build_at_a_time = 3 
total_hosts = hosts_to_build.keys.length
current_count = 0 
total_count = 0
hosts_to_build.each do |host_type, host_data|
  total_count += 1
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
    puts "Building #{host_type}"
    ENV['PATH'] = "#{ENV['PATH']}:/Library/Application Support/VMware Fusion"
    vm_path = "#{VM_BASE_PATH}/#{host_data["vm"]}"
    begin
      run_command "vmrun start '#{vm_path}'"
      run_command "vmrun -gu root -gp opscode runProgramInGuest '#{vm_path}' /root/omnibus/build-omnibus.sh #{PROJECT} #{BUCKET} '#{S3_ACCESS_KEY}' '#{S3_SECRET_KEY}'"
      run_command "vmrun -gu root -gp opscode CopyFileFromGuestToHost '#{vm_path}' /tmp/omnibus.out '#{BASE_PATH}/build-output/#{host_type}.out'"
    ensure
      run_command "vmrun stop '#{vm_path}'"
    end
    exit 0
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

