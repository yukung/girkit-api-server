/*
 * Copyright 2015 Yusuke Ikeda
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import org.apache.commons.lang.RandomStringUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.yukung.girkit.App
import org.yukung.girkit.InternetAPI
import ratpack.error.ClientErrorHandler
import ratpack.error.ServerErrorHandler
import ratpack.exec.Blocking
import ratpack.handling.Context

import static ratpack.groovy.Groovy.ratpack

final Logger log = LoggerFactory.getLogger(Ratpack)

ratpack {
    bindings {
        bindInstance ClientErrorHandler, new ClientErrorHandler() {
            @Override
            void error(Context context, int statusCode) throws Exception {
                log.warn "status: ${statusCode}, method: ${context.request.method}, path: ${context.request.path}"
                context.response.status(statusCode).send "${context.response.status.message}"
            }
        }
        bindInstance ServerErrorHandler, new ServerErrorHandler() {
            @Override
            void error(Context context, Throwable throwable) throws Exception {
                log.warn "status: ${context.response.status}, method: ${context.request.method}, path: ${context.request.path}", throwable
                context.response.status(500).send("Error: ${throwable.message}")
            }
        }
    }

    handlers {
        def token = System.env.SECRET_TOKEN ?: RandomStringUtils.randomAlphanumeric(32)
        log.info("URL path token : {}", token)

        prefix("${token}/api") {
            all {
                context.response.headers.add 'Access-Control-Allow-Origin', '*'
                next()
            }
            post(":device/:commands") {
                def device = pathTokens['device']
                def info = App.data['Device'][device]
                def irkit = new InternetAPI(clientKey: info.clientkey, deviceId: info.deviceid)

                def commands = pathTokens['commands'].split(',')
                commands.eachWithIndex { command, i ->
                    def irData = App.data['IR'][command]
                    def res = irkit.postMessages(irData)
                    if (res.status == 200) {
                        log.info "Success: ${command} to ${device}"
                    }

                    if (i < commands.size() - 1) Blocking.exec { sleep 1000 }
                }
            }
        }
    }
}
