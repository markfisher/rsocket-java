/*
 * Copyright 2016 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.rsocket.examples.transport.tcp.requestresponse;

import io.rsocket.AbstractRSocket;
import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.RSocketFactory;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.transport.netty.server.TcpServerTransport;
import io.rsocket.util.PayloadImpl;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

public final class HelloWorldClient {

    public static void main(String[] args) {
        RSocketFactory
            .receive()
            .acceptor((setupPayload, reactiveSocket) ->
                Mono.just(new AbstractRSocket() {
                    boolean fail = true;

                    @Override
                    public Mono<Payload> requestResponse(Payload p) {
                        if (fail) {
                            fail = false;
                            return Mono.error(new Throwable());
                        } else {
                            return Mono.just(p);
                        }
                    }
                })
            )
            .transport(TcpServerTransport.create("localhost", 7000))
            .start()
            .subscribe();

        RSocket socket =
            RSocketFactory
                .connect()
                .transport(TcpClientTransport.create("localhost", 7000))
                .start()
                .block();

        socket.requestResponse(new PayloadImpl("Hello"))
            .map(payload -> StandardCharsets.UTF_8.decode(payload.getData()).toString())
            .onErrorReturn("error")
            .doOnNext(System.out::println)
            .block();

        socket.requestResponse(new PayloadImpl("Hello"))
            .map(payload -> StandardCharsets.UTF_8.decode(payload.getData()).toString())
            .onErrorReturn("error")
            .doOnNext(System.out::println)
            .block();

        socket.requestResponse(new PayloadImpl("Hello"))
            .map(payload -> StandardCharsets.UTF_8.decode(payload.getData()).toString())
            .onErrorReturn("error")
            .doOnNext(System.out::println)
            .block();

        socket.close().block();
    }
}
