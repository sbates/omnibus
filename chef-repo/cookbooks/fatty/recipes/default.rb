#
# Cookbook Name:: fatty
# Recipe:: default
#
# Copyright 2011, Opscode, Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

include_recipe "build-essential"
include_recipe "git"
include_recipe "leiningen"

package "libtool"

case node[:platform]
when "debian", "ubuntu"
  package "dpkg-dev"
  package "libxml2"
  package "libxml2-dev"
  package "libxslt1.1"
  package "libxslt1-dev"
when "centos", "redhat", "fedora"
  package "rpm-build"
  package "libxml2"
  package "libxml2-devel"
  package "libxslt"
  package "libxslt-devel"
end

gem_package "fpm"
gem_package "fog"

git "/tmp/fatty" do
  repository "git://github.com/adamhjk/fatty.git"
  reference "master"
  action :sync
  notifies :run, "execute[lein deps]", :immediately
end

execute "lein deps" do
  cwd "/tmp/fatty"
  environment "LEIN_ROOT" => "1", "LEIN_HOME" => "/tmp/fatty/.lein"
  action :nothing
end

directory "/tmp/fatty/build" do
  mode "0755"
  action :create
end

execute "build-chef-full" do
  cwd "/tmp/fatty"
  environment "LEIN_ROOT" => "1", "LEIN_HOME" => "/tmp/fatty/.lein"
  command "lein run --project-name 'chef-full'"
end

ruby_block "store in s3" do
  block do
    Gem.clear_paths
    require 'fog'
    connection = Fog::Storage.new(
      :provider => 'AWS',
      :aws_access_key_id => node[:fatty][:aws_access_key_id],
      :aws_secret_access_key => node[:fatty][:aws_secret_access_key]
    )
    directory = connection.directories.create(:key => "opscode-full-stack", :public => true)
    Dir["/tmp/fatty/pkg/*"].each do |filename|
      file = directory.files.create(:key => "#{node[:platform]}-#{node[:platform_version]}-#{node[:kernel][:machine]}/#{File.basename(filename)}", :body => File.open(filename), :public => true)
    end
  end
end
