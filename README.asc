= Working with Spring Data Couchbase
Laurent Doguin <laurent.doguin@couchbase.com >
Josh Long <josh@joshlong.com>
v1.0, 2014-10-05

:toc:

This is a demo of the https://github.com/spring-projects/spring-data-couchbase[Spring Data Couchbase] integration.

From http://spring.io/projects/spring-data-couchbase[the project page], Spring Data Couchbase is:


============================================================================
The Spring Data Couchbase project provides integration with the Couchbase Server database. Key functional areas of Spring Data Couchbase are a POJO centric model for interacting with Couchbase Buckets and easily writing a Repository style data access layer.
============================================================================



==  The Demo

The example demonstrates *activity* objects. Before running this example, setup views on the Couchbase servers. Go to Couchbase admin console and create a new design document called 'activity' in the default bucket. You then need two views.

View `all`:

[source,javascript]
----
function (doc, meta) {
  emit(meta.id, null);
}
----


View `byOrigin`

[source,javascript]
----
function (doc, meta) {
  if(doc._class == "demo.Activity" && doc.origin) {
    emit(doc.origin, null);
  }
}
----

== Setting up a cluster with Vagrant

You will need to have Couchbase installed if you don't already (naturally). Michael Nitschinger (https://twitter.com/daschl[@daschl], also lead of the Spring Data Couchbase project), blogged about how to get a simple  http://blog.couchbase.com/couchbase-cluster-minutes-vagrant-and-puppet[4-node Vagrant cluster up and running here]. I've reproduced his example here in the  `vagrant` directory. To use it, you'll need to install https://www.virtualbox.org/[Virtual Box] and https://www.vagrantup.com/[Vagrant], of course, but then simply run `vagrant up` in the `vagrant` directory. To get the most up-to-date version of this configuration script, I went to https://github.com/daschl/vagrants[Michael's GitHub `vagrants` project] and found that, beyond this example, there are https://github.com/couchbaselabs/vagrants[_numerous_ other Vagrant scripts available]. 

You can then point your browser to 192.168.56.10[1-4] and work with   your Couchbase cluster. You'll need to administer and configure it on setup, too, so point your browser to 192.168.56.10[1-4]:8091. For more on that process, I recommend that you follow the http://docs.couchbase.com/prebuilt/couchbase-manual-3.0/Install/init-setup.html[guidelines here] for the details. Starting with `192.168.56.101`, I  went to each node at their respective admin interfaces and created a new cluster. I used `admin` for the username and `password` for the password. On all subsequent management pages, I simply joined the existing cluster by pointing the nodes to `192.168.56.101` and using the aforemention `admin` credential. Once you've joined all nodes, look for the `Rebalance` button in the server config panel and trigger a cluster rebalance.

If you are done with your Vagrant cluster, you can use the `vagrant halt` command to shut it down cleanly. Very handy is also `vagrant suspend`, which will save the state of the nodes instead of shutting them down completely.

If you want to administer the Couchbase cluster from the command line there is the handy  http://docs.couchbase.com/couchbase-manual-2.5/cb-cli/#couchbase-cli-tool[`couchbase-cli`]. You can simply use the `vagrant ssh` command to get into each of the nodes (by their node-names: `node1`, `node2`, etc..). Once there, you can run cluster configuration commands. For example the `server-list` command will enumerate cluster nodes.

[source,sh]
----
/opt/couchbase/bin/couchbase-cli server-list -c 192.168.56.101 -u admin -p password
----

It's easy to trigger a *rebalance* using:

[source,sh]
----
/opt/couchbase/bin/couchbase-cli rebalance -c 192.168.56.101 -u admin -p password
----
