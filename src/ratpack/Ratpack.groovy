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


import com.github.seratch.jslack.Slack
import com.github.seratch.jslack.api.model.Attachment
import com.github.seratch.jslack.api.webhook.Payload
import org.apache.commons.lang3.RandomStringUtils
import org.codehaus.groovy.runtime.StackTraceUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.yukung.girkit.App
import org.yukung.girkit.InternetAPI
import ratpack.error.ClientErrorHandler
import ratpack.error.ServerErrorHandler

import java.time.ZonedDateTime

import static ratpack.groovy.Groovy.*

final Logger log = LoggerFactory.getLogger(Ratpack)

ratpack {
    bindings {
        bindInstance ClientErrorHandler, { context, statusCode ->
            log.warn "status: ${statusCode}, method: ${context.request.method}, path: ${context.request.path}"
            context.response.status(statusCode).send "${context.response.status.message}"
        } as ClientErrorHandler

        bindInstance ServerErrorHandler, { context, throwable ->
            log.error("status: ${context.response.status}, method: ${context.request.method}, path: ${context.request.path}",
                StackTraceUtils.deepSanitize(throwable)
            )
            context.response.status(500).send(
                "Error: ${throwable.message}, IRKit response: ${throwable.response.status} ${throwable.response.data}"
            )
        } as ServerErrorHandler
    }

    handlers {
        def token = System.env.SECRET_TOKEN ?: RandomStringUtils.randomAlphanumeric(32)
        log.debug("URL path token : ${token}")

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
                def (successes, attachments) = [[], []]
                def webhookUrl = System.env.SLACK_WEBHOOK_URL

                commands.eachWithIndex { command, i ->
                    def irData = App.data['IR'][command]
                    def res = irkit.postMessages irData
                    if (res.statusCode == 200) {
                        log.info "Success: ${command} to ${device}"
                        if (webhookUrl) attachments << attachment(device, command)
                        successes << command
                    }
                    if (i < commands.size() - 1) sleep 1000
                }
                if (webhookUrl) Slack.getInstance().send webhookUrl, payload(attachments)
                context.response.send "successful commands: ${successes.join(',')}"
            }
        }
    }
}

def payload(attachments) {
    Payload.builder()
        .text("Sent the signal to the IRKit :on:")
        .attachments(attachments)
        .build()
}

def attachment(device, command) {
    Attachment.builder()
        .color("good")
        .mrkdwnIn(["text"])
        .authorName(device)
        .title("Command: ${command}")
        .text("The command [*${command}*] was successfully sent to IRKit :ok:")
        .footer("IRKit API Server")
        .ts("${ZonedDateTime.now().toInstant().toEpochMilli() / 1000}")
        .build()
}
