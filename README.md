Strixy
======

A simple HTTP proxy that changes some response headers.
Useful to allow JavaScript to make CORS requests to sites that does too restrictive headers.
Also removes X-Frame-Options header to allow embedding a site as a iframe.

Usage
-----

Let's say you want to access `http://example.com/secret.json` then simply change the URL to 
`http://strixy/http://example.com/secret.json` to proxy the request through Strixy.
