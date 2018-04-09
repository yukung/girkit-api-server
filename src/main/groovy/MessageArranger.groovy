import com.github.seratch.jslack.api.model.Attachment
import com.github.seratch.jslack.api.webhook.Payload

import java.time.ZonedDateTime

@Singleton
class MessageArranger {
    static def attachment(device, command) {
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

    static def payload(attachments) {
        Payload.builder()
            .text("Sent the signal to the IRKit :on:")
            .attachments(attachments)
            .build()
    }
}
