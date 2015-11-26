om-next-e2e
===========

Example of om.next end to end app with frontend communicating with backend

Guide
-----

Setup
.....

Start by creating a new clojure project with leiningen::

    lein new om-next-e2e

Basic Logging and HTTP Server
.............................

* `Add the dependencied <https://github.com/marianoguerra-atik/om-next-e2e/commit/32842e95abc4960b32488a51110fe7d7e385be88#diff-0fff143854a4f5c0469a3819b978a483R9>`_
* `Expose basic API <https://github.com/marianoguerra-atik/om-next-e2e/commit/32842e95abc4960b32488a51110fe7d7e385be88#diff-07922f5b56cc381777c62c1d0016830eR1>`_

Jump to this commit with::

    git checkout 32842e95abc4960b32488a51110fe7d7e385be88

To test run::

    lein run

You should see::

    14:55:22.179 [main] INFO  om-next-e2e.core - Starting Server at
    localhost:8080/
    14:55:22.778 INFO  [org.projectodd.wunderboss.web.Web] (main) Registered
    web context /

On another terminal using httpie (httpie.org)::

    $ http get localhost:8080/

    HTTP/1.1 200 OK
    Connection: keep-alive
    Content-Length: 12
    Date: Thu, 26 Nov 2015 13:55:24 GMT
    Server: undertow

    Hello world!

Basic Routing with Bidi
.......................

* `Add bidi dependency for routing <https://github.com/marianoguerra-atik/om-next-e2e/commit/03b95c397b1c7d21cafe7a9a21efebc7df5b6b41#diff-0fff143854a4f5c0469a3819b978a483R9>`_
* `Setup basic routing <https://github.com/marianoguerra-atik/om-next-e2e/commit/03b95c397b1c7d21cafe7a9a21efebc7df5b6b41#diff-07922f5b56cc381777c62c1d0016830eR3>`_

  + We expose two endpoints (POST /action and POST /query) 
  + We add a catch all not found handler

This handlers (action and query) just return 200 and the body with some extra
content.

Jump to this commit with::

    git checkout 03b95c397b1c7d21cafe7a9a21efebc7df5b6b41

Let's try it, first let's try the not found handler::

    $ http get localhost:8080/lala
    HTTP/1.1 404 Not Found
    Content-Length: 9
    Server: undertow

    Not Found

Let's check that doing get on a route that handles only post returns 404 (for REST purists it should be 405, I know)::

    $ http get localhost:8080/action
    HTTP/1.1 404 Not Found
    Content-Length: 9
    Server: undertow

    Not Found

Let's send some content to action as json for now::

    $ http post localhost:8080/action name=lala
    HTTP/1.1 200 OK
    Content-Length: 24
    Server: undertow

    action: {"name": "lala"}

And query::

    $ http post localhost:8080/query name=lala
    HTTP/1.1 200 OK
    Content-Length: 30
    Server: undertow

    query action: {"name": "lala"}

Use Transit for Requests and Responses
......................................

* `Add the transit dependency <https://github.com/marianoguerra-atik/om-next-e2e/commit/56d8d2e615e7f499c9dbeaa1d1479a0f39dc1950#diff-0fff143854a4f5c0469a3819b978a483R10>`_
* `Deserialize transit on the way in and serialize it on the way out <https://github.com/marianoguerra-atik/om-next-e2e/commit/56d8d2e615e7f499c9dbeaa1d1479a0f39dc1950#diff-07922f5b56cc381777c62c1d0016830eR4>`_

Jump to this commit with::

    git checkout 56d8d2e615e7f499c9dbeaa1d1479a0f39dc1950

From here on I will use a tool I created called `transito
<https://pypi.python.org/pypi/transito>`_ written in python since writing and
reading transit is not fun I created a tool to translate to and from json,
transit and edn, here I use edn since it's more readable and is what we will
use in our om.next frontend, you can install it with::

    sudo pip install transito

https://pypi.python.org/pypi/transito

Send an action::

    $ echo '(start {:id "id2"})' | transito http post http://localhost:8080/action e2t -

    Status: 200
    Content-Type: application/transit+json
    Content-Length: 60
    Server: undertow

    {:action (start {:id "id2"})}

The response is translated from transit to edn, the actual response can be seen
using something like curl::

    curl -X POST http://localhost:8080/action -d '["~#list",["~$start",["^ ","~:id","id2"]]]'

    ["^ ","~:action",["~#list",["~$start",["^ ","~:id","id2"]]]]

You can get the body you want translated to transit like this::

    echo '(start {:id "id2"})' | transito e2t -
    ["~#list",["~$start",["^ ","~:id","id2"]]]

Let's try the not found handler (notice we are sending to actiona instead of action)::

    $ echo '(start {:id "id2"})' | transito http post http://localhost:8080/actiona e2t -
    Status: 404
    Content-Type: application/transit+json
    Content-Length: 28
    Server: undertow

    {:error "Not Found"}

Now let's test the query endpoint::

    $ echo '(tasks {:id "id2"})' | transito http post http://localhost:8080/query e2t -
    Status: 200
    Content-Type: application/transit+json
    Content-Length: 59
    Server: undertow

    {:query (tasks {:id "id2"})}

Supporting Actions and Queries
..............................

At this point we need to support the same mutations and reads as the frontend,
to do this we need to add the om.next dependency, I'm using om next alpha25 SNAPSHOT,
here is the way to install the exact version I'm using::

    git clone https://github.com/omcljs/om.git
    cd om
    git checkout 34b9a614764f47a022ddfaf2e469d298d7605d44
    lein install

Then:

* `Add the om dependency <https://github.com/marianoguerra-atik/om-next-e2e/commit/f9ac70c18c89ecbe336c736ef266c17ee1ef8eab#diff-0fff143854a4f5c0469a3819b978a483R8>`_
* `Define multimethods for read and mutate <https://github.com/marianoguerra-atik/om-next-e2e/commit/f9ac70c18c89ecbe336c736ef266c17ee1ef8eab#diff-d527e7a759eae73907536b425c95666eR10>`_
* `Create the om parser <https://github.com/marianoguerra-atik/om-next-e2e/commit/f9ac70c18c89ecbe336c736ef266c17ee1ef8eab#diff-d527e7a759eae73907536b425c95666eR13>`_
* `Implement readers <https://github.com/marianoguerra-atik/om-next-e2e/commit/f9ac70c18c89ecbe336c736ef266c17ee1ef8eab#diff-d527e7a759eae73907536b425c95666eR41>`_

  + :count will return the current count
  + :default will return :not-found

* `Implement mutators <https://github.com/marianoguerra-atik/om-next-e2e/commit/f9ac70c18c89ecbe336c736ef266c17ee1ef8eab#diff-d527e7a759eae73907536b425c95666eR47>`_

  + 'increment will increment the counter by the value passed as parameter
  + :default will return an error

* `Change the action handler to use the mutators <https://github.com/marianoguerra-atik/om-next-e2e/commit/f9ac70c18c89ecbe336c736ef266c17ee1ef8eab#diff-d527e7a759eae73907536b425c95666eR16>`_ 
* `Change the query handler to use the readers <https://github.com/marianoguerra-atik/om-next-e2e/commit/f9ac70c18c89ecbe336c736ef266c17ee1ef8eab#diff-d527e7a759eae73907536b425c95666eR25>`_

Jump to this commit with::

    git checkout f9ac70c18c89ecbe336c736ef266c17ee1ef8eab

Now let's test it.

Increment by 20::

    $ echo '(increment {:value 20})' | transito http post http://localhost:8080/action e2t -

    Status: 200
    Content-Type: application/transit+json
    Content-Length: 44
    Server: undertow

    {:value {:keys [:count]}}

Get current count::

    $ echo '[:count]' | transito http post http://localhost:8080/query e2t -

    Status: 200
    Content-Type: application/transit+json
    Content-Length: 19
    Server: undertow

    {:count 20}

Increment by 1::

    $ echo '(increment {:value 1})' | transito http post http://localhost:8080/action e2t -

    Status: 200
    Content-Type: application/transit+json
    Content-Length: 44
    Server: undertow

    {:value {:keys [:count]}}

Get current count::

    $ echo '[:count]' | transito http post http://localhost:8080/query e2t -

    Status: 200
    Content-Type: application/transit+json
    Content-Length: 19
    Server: undertow

    {:count 21}

Try getting something else to try the :default handler::

    $ echo '[:otherthing]' | transito http post http://localhost:8080/query e2t -

    Status: 200
    Content-Type: application/transit+json
    Content-Length: 6
    Server: undertow

    {}

Try an inexistent action to try the :default handler::

    $ echo '(somethingelse {:value 1})' | transito http post http://localhost:8080/action e2t -

    Status: 404
    Content-Type: application/transit+json
    Content-Length: 84
    Server: undertow

    {:params {:value 1}, :key somethingelse, :error "Not Found"}

License
-------

Copyright © 2015 Mariano Guerra

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
