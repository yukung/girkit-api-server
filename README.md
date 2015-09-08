IRKit API Server for Groovy
===========================

Description
-----------

This is the API server for sharing IR data of IRKit. I made by being inspired by [riywo/irkit-api-server](https://github.com/riywo/irkit-api-server).

Usage
-----

You can use the following procedure.

1. Create IR data file(`irkit.json`)
2. Upload the IR data somewhere else.
3. Deploy API server.

### Create IR data

Please use it to deploy to any environment which is running Java such as Heroku. Before deploying, please the IR data file created using the [`girkit CLI`](https://github.com/yukung/girkit).

As below:

```shell-session
$ girkit --get tv_on
$ girkit --get tv_off
$ girkit --get airconditioner_on
$ girkit --get airconditioner_off
$ girkit --device:add myroom
$ ls ~/.irkit.json
/Users/yukung/.irkit.json
```

#### Compatibility

For IR Data files(`irkit.json`) that have it compatible with output from the `ruby-irkit`, it does not matter with a `ruby-irkit`.

### Upload IR data

You need to server the public accessible web server `irkit.json`. because it contains some private information to `irkit.json`, please be placed in hard to guess the location. Such as Private Gist and Dropbox share link would be better.

### Deploy API server

You can deploy via Heroku deploy button. You need to specify the URL that was arranged `irkit.json` the `IRKIT_DATA_FILE`in deployment screen of Heroku.

After you deploy, please check the `SECRET_TOKEN` of environment variable. Access destination of the URL is confirmed.

You can now in available!! if you send an HTTP request as follows:

```console
$ curl -X POST https://your-app-name.herokuapp.com/SECRET_TOKEN/api/myroom/tv_on
```

Author
------

[@yukung](https://twitter.com/yukung)

License
-------

Licensed under the terms of the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html)