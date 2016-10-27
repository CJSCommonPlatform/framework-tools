package uk.gov.justice.replay.event.replayer;

import static java.util.UUID.randomUUID;
import static java.util.stream.Stream.of;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;
import static uk.gov.justice.services.messaging.DefaultJsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithRandomUUID;

import uk.gov.justice.services.common.converter.ObjectToJsonValueConverter;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.core.accesscontrol.AccessControlFailureMessageGenerator;
import uk.gov.justice.services.core.accesscontrol.AccessControlService;
import uk.gov.justice.services.core.accesscontrol.AllowAllPolicyEvaluator;
import uk.gov.justice.services.core.accesscontrol.PolicyEvaluator;
import uk.gov.justice.services.core.annotation.Adapter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.cdi.LoggerProducer;
import uk.gov.justice.services.core.dispatcher.DispatcherCache;
import uk.gov.justice.services.core.dispatcher.DispatcherFactory;
import uk.gov.justice.services.core.dispatcher.EmptySystemUserProvider;
import uk.gov.justice.services.core.dispatcher.RequesterProducer;
import uk.gov.justice.services.core.dispatcher.ServiceComponentObserver;
import uk.gov.justice.services.core.dispatcher.SystemUserUtil;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.eventfilter.AllowAllEventFilter;
import uk.gov.justice.services.core.extension.AnnotationScanner;
import uk.gov.justice.services.core.extension.BeanInstantiater;
import uk.gov.justice.services.core.interceptor.InterceptorCache;
import uk.gov.justice.services.core.interceptor.InterceptorChainProcessor;
import uk.gov.justice.services.core.interceptor.InterceptorChainProcessorProducer;
import uk.gov.justice.services.core.jms.DefaultJmsDestinations;
import uk.gov.justice.services.core.jms.JmsSenderFactory;
import uk.gov.justice.services.core.sender.ComponentDestination;
import uk.gov.justice.services.core.sender.SenderProducer;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.JsonObjectEnvelopeConverter;
import uk.gov.justice.services.messaging.jms.DefaultJmsEnvelopeSender;
import uk.gov.justice.services.messaging.jms.EnvelopeConverter;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.openejb.jee.WebApp;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Classes;
import org.apache.openejb.testing.Jars;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ApplicationComposer.class)
@Adapter(EVENT_LISTENER)
public class EventHandlerIT {

    @Module
    @Jars("deltaspike-")
    @Classes(cdi = true, value = {

            // replayer classes
            EventReplayer.class,
            EventDispatcher.class,

            // event-listener classes
            MyEventListener.class,

            // microservices core classes
            AnnotationScanner.class,
            RequesterProducer.class,
            ServiceComponentObserver.class,
            AllowAllEventFilter.class,

            InterceptorChainProcessorProducer.class,
            InterceptorChainProcessor.class,
            InterceptorCache.class,

            SenderProducer.class,
            JmsSenderFactory.class,
            ComponentDestination.class,
            DefaultJmsEnvelopeSender.class,
            DefaultJmsDestinations.class,
            EnvelopeConverter.class,

            StringToJsonObjectConverter.class,
            JsonObjectEnvelopeConverter.class,
            ObjectToJsonValueConverter.class,
            ObjectMapper.class,
            Enveloper.class,

            AccessControlFailureMessageGenerator.class,
            AllowAllPolicyEvaluator.class,
            AccessControlService.class,
            DispatcherCache.class,
            DispatcherFactory.class,
            PolicyEvaluator.class,

            LoggerProducer.class,
            EmptySystemUserProvider.class,
            SystemUserUtil.class,
            BeanInstantiater.class
    })
    public WebApp war() {
        return new WebApp()
                .contextRoot("replay-war");
    }

    @ApplicationScoped
    @ServiceComponent(EVENT_LISTENER)
    public static class MyEventListener {

        private JsonEnvelope eventEnvelope;

        @Handles("my.event")
        public void setEventEnvelope(final JsonEnvelope eventEnvelope){
            this.eventEnvelope = eventEnvelope;
        }

        public JsonEnvelope getEventEnvelope() {
            return eventEnvelope;
        }
    }

    @Inject
    private EventReplayer eventReplayer;

    @Inject
    private MyEventListener myEventListener;

    @Test
    public void shouldDispatchTheEventToTheCorrectEventListenerOnTheCLasspath() throws Exception {

        final JsonEnvelope eventEnvelope = createEventEnvelope("my.event");

        eventReplayer.replay(of(eventEnvelope));

        assertThat(myEventListener.getEventEnvelope(), is(sameInstance(eventEnvelope)));
    }

    private JsonEnvelope createEventEnvelope(final String eventName) {

        return envelopeFrom(
                metadataWithRandomUUID(eventName)
                        .withClientCorrelationId(randomUUID().toString())
                        .withSessionId(randomUUID().toString())
                        .withUserId(randomUUID().toString())
                        .withStreamId(randomUUID())
                        .build(),
                createObjectBuilder()
                        .add("subscriptionId", randomUUID().toString())
                        .build());
    }
}

