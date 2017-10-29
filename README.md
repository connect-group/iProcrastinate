# iProcrastinate
<blockquote>Simulate a slow HTTP Service</blockquote>

## Basic Usage
This simple web service allows you to simulate a slow response from a web service.
By default, the service will simply return a basic JSON response.


    { "result": "OK" }


The service will respond according to the number of milliseconds requested, for example,
`http://{host}/wait/1000` will wait for 1 second.



You can also specify a range, in which case the service will choose a random time within the specified range.
`http://{host}/wait/1000-5000` will respond between 1 and 5 seconds
later.

## Redirects
Instead of returning an "OK" response, the system can be configured to redirect to a web page after the specified delay.
Simply add the query string "?redirect=...." to have the system send a redirect response.

A simple request would be `http://{host}/wait/1000-5000?redirect=http://www.google.com`.

The URL must be properly encoded.  For example, to get timezone information from `http://api.geonames.org/timezoneJSON?formatted=true&lat=47.01&lng=10.2&username=demo&style=full`,
would encode as `/wait/1000?redirect=http%3A%2F%2Fapi.geonames.org%2FtimezoneJSON%3Fformatted%3Dtrue%26lat%3D47.01%26lng%3D10.2%26username%3Ddemo%26style%3Dfull`.

The service will respond to a HTTP POST request with a 307 response code; compatible clients will then re-post
the data to the new location.

## Performance
The service is built on Tomcat 8.5/Servlet 3.1 in asynchronous mode. The intention is that the system will handle
a large number of concurrent requests.  Part of the aim of the project was to explore Async IO and Async
Servlets.

## CORS
CORS can be added to Tomcat using a Filter in the web.xml.


    <filter>
        <filter-name>CorsFilter</filter-name>
        <filter-class>org.apache.catalina.filters.CorsFilter</filter-class>
        <async-supported>true</async-supported>
        <init-param>
            <param-name>cors.allowed.origins</param-name>
            <param-value>*</param-value>
        </init-param>
        <init-param>
            <param-name>cors.allowed.methods</param-name>
            <param-value>GET,POST,HEAD,OPTIONS</param-value>
        </init-param>
        <init-param>
            <param-name>cors.allowed.headers</param-name>
            <param-value>Content-Type,X-Requested-With,accept,Origin,Access-Control-Request-Method,Access-Control-Request-Headers</param-value>
        </init-param>
        <init-param>
            <param-name>cors.exposed.headers</param-name>
            <param-value>Access-Control-Allow-Origin,Access-Control-Allow-Credentials</param-value>
        </init-param>
        <init-param>
            <param-name>cors.support.credentials</param-name>
            <param-value>true</param-value>
        </init-param>
        <init-param>
            <param-name>cors.preflight.maxage</param-name>
            <param-value>1800</param-value>
        </init-param>
    </filter>
    <filter-mapping>
        <filter-name>CorsFilter</filter-name>
        <url-pattern>/*</url-pattern>
    </filter-mapping>



## See Also<
See also [http://slowwly.robertomurray.co.uk](http://slowwly.robertomurray.co.uk) which provides a
similar service using Ruby.
