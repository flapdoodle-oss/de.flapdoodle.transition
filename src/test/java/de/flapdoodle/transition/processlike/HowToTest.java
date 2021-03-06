/**
 * Copyright (C) 2016
 *   Michael Mosmann <michael@mosmann.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.flapdoodle.transition.processlike;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.ClassRule;
import org.junit.Test;

import de.flapdoodle.testdoc.Recorder;
import de.flapdoodle.testdoc.Recording;
import de.flapdoodle.testdoc.TabSize;
import de.flapdoodle.transition.StateID;
import de.flapdoodle.transition.routes.Bridge;
import de.flapdoodle.transition.routes.End;
import de.flapdoodle.transition.routes.PartingWay;
import de.flapdoodle.transition.routes.Route;
import de.flapdoodle.transition.routes.RoutesAsGraph;
import de.flapdoodle.transition.routes.SingleSource;
import de.flapdoodle.transition.routes.Start;
import de.flapdoodle.types.Either;

public class HowToTest {
	@ClassRule
	public static Recording recording = Recorder.with("HowToBuildAnProcessEngine.md", TabSize.spaces(2));

	@Test
	public void vertex() {
		recording.begin();
		StateID<String> id = StateID.of(String.class);
		StateID<String> idWithLabel = StateID.of("foo", String.class);
		recording.end();
	}

	@Test
	public void edges() {
		recording.begin();
		Start<String> start;
		Bridge<String, String> bridge;
		PartingWay<String, String, String> parting;
		End<String> end;

		start = Start.of(StateID.of(String.class));
		bridge = Bridge.of(StateID.of("a", String.class), StateID.of("b", String.class));
		parting = PartingWay.of(StateID.of("start", String.class), StateID.of("oneDestination", String.class),
				StateID.of("otherDestination", String.class));
		end = End.of(StateID.of("start", String.class));
		recording.end();
	}

	@Test
	public void state() {
		recording.begin();
		State<String> state = State.of(StateID.of("foo", String.class), "hello");
		recording.end();
	}

	@Test
	public void listener() {
		recording.begin();
		ProcessListener listener = ProcessListener.builder()
				.onStateChange((Optional<? extends State<?>> route, State<?> currentState) -> {

				})
				.onStateChangeFailedWithRetry((Route<?> currentRoute, Optional<? extends State<?>> lastState) -> {
					// decide, if thread should sleep some time
				})
				.build();
		recording.end();
	}

	@Test
	public void startAndEnd() {
		AtomicReference<String> result = new AtomicReference<>();
		List<State<?>> states = new ArrayList<>();

		recording.begin();
		ProcessRoutes<SingleSource<?, ?>> routes = ProcessRoutes.builder()
				.add(Start.of(StateID.of(String.class)), () -> "foo")
				.add(End.of(StateID.of(String.class)), i -> {
					result.set(i);
				})
				.build();

		ProcessEngineLike pe = ProcessEngineLike.with(routes);

		ProcessListener listener = ProcessListener.builder()
				.onStateChange((route, currentState) -> {
					states.add(currentState);
				})
				.onStateChangeFailedWithRetry((currentRoute, lastState) -> {
					throw new IllegalArgumentException("should not happen");
				})
				.build();

		pe.run(listener);
		recording.end();

		assertEquals("foo", result.get());
		assertEquals(1, states.size());
		assertEquals("foo", states.get(0).value());
	}

	@Test
	public void startBridgeAndEnd() {
		AtomicReference<Integer> result = new AtomicReference<>();
		recording.begin();
		ProcessRoutes<SingleSource<?, ?>> routes = ProcessRoutes.builder()
				.add(Start.of(StateID.of(String.class)), () -> "12")
				.add(Bridge.of(StateID.of(String.class), StateID.of(Integer.class)), a -> Integer.valueOf(a))
				.add(End.of(StateID.of(Integer.class)), i -> {
					result.set(i);
				})
				.build();

		ProcessEngineLike pe = ProcessEngineLike.with(routes);

		pe.run(ProcessListener.noop());
		recording.end();

		assertEquals(Integer.valueOf(12), result.get());
	}

	@Test
	public void loopSample() {
		List<Object> values = new ArrayList<>();

		recording.begin();
		ProcessRoutes<SingleSource<?, ?>> routes = ProcessRoutes.builder()
				.add(Start.of(StateID.of("start", Integer.class)), () -> 0)
				.add(Bridge.of(StateID.of("start", Integer.class), StateID.of("decide", Integer.class)), a -> a + 1)
				.add(PartingWay.of(StateID.of("decide", Integer.class), StateID.of("start", Integer.class),
						StateID.of("end", Integer.class)), a -> a < 3 ? Either.left(a) : Either.right(a))
				.add(End.of(StateID.of("end", Integer.class)), i -> {
					values.add(i);
				})
				.build();

		ProcessEngineLike pe = ProcessEngineLike.with(routes);

		ProcessListener listener = ProcessListener.builder()
				.onStateChange((route, currentState) -> {
					if (currentState.type().name().equals("decide")) {
						values.add(currentState.value());
					}
				})
				.build();

		pe.run(listener);

		String dot = RoutesAsGraph.routeGraphAsDot("simpleLoop", RoutesAsGraph.asGraphIncludingStartAndEnd(routes.all()));
		recording.end();

		recording.output("dotFile", dot);

		assertEquals("[1, 2, 3, 3]", values.toString());
	}


}
