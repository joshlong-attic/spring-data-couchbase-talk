Spring  Data Couchbase talk
==========================

This is a demo showing the use of spring-data-couchbase.

It currently handles activity objects. To make sure things work correctly you need to setup views on the server side. Go
to Couchbase admin console and create a new design document called 'activity' in the default bucket. You then need two
views.

View 'all'

    function (doc, meta) {
      emit(meta.id, null);
    }


View 'byOrigin'

    function (doc, meta) {
      if(doc._class == "springCouchbase.domain.Activity" && doc.origin) {
        emit(doc.origin, null);
      }
    }

