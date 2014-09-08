package fr.xebia.sims

import org.vertx.groovy.platform.Verticle

class SimsFactoryVerticle extends Verticle {

    def start() {

        def me = [
                id  : UUID.randomUUID().toString(),
                type: 'factory',
        ]

        def channels = [
                output: [
                        status : me.type + '.status',
                        consume: me.type + '.consume',
                        consume: me.type + '.production'
                ]
        ]

        def conf = [
                powerPerUnit: (container.config[me.type]?.maxLoad ?: 5).toInteger(),
                startChannel: 'monitoring.service.start'
        ]

        vertx.eventBus.registerHandler(channels.output.status) { message ->
            println "OUTPUT ${channels.output.status} => ${message.body}"
        }

        vertx.eventBus.registerHandler(conf.startChannel) { message ->
            println "OUTPUT ${conf.startChannel} => ${message.body}"
        }

        vertx.eventBus.registerHandler(me.type + 'consume.' + me.id) { message ->
            println "OUTPUT ${channels.output.status} => ${message.body}"
        }

        def emit = { String busAddress, Map message ->
            vertx.eventBus.publish(busAddress, me + message)
        }

        emit(conf.startChannel, channels)

        vertx.setPeriodic(750) { timerID ->
            emit('power.plant.consume', [replyTo: me.type + 'consume.' + me.id, need: 4])
        }
    }
}
