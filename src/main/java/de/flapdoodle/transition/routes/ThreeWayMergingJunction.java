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
package de.flapdoodle.transition.routes;

import java.util.Set;

import org.immutables.value.Value;

import de.flapdoodle.transition.NamedType;
import de.flapdoodle.transition.State;

@Value.Immutable
public interface ThreeWayMergingJunction<L,M,R,D> extends SingleDestination<D> {
	NamedType<L> left();
	NamedType<M> middle();
	NamedType<R> right();
	
	@Override
	default Set<NamedType<?>> sources() {
		return NamedType.setOf(left(),middle(),right());
	}

	
	interface Transition<L,M,R,D> extends Route.Transition<D> {
		State<D> apply(State<L> t, State<M> m, State<R> r);
	}

	public static <L,M,R,D> ThreeWayMergingJunction<L,M,R,D> of(NamedType<L> left, NamedType<M> middle, NamedType<R> right, NamedType<D> destination) {
		return ImmutableThreeWayMergingJunction.<L,M,R,D>builder(destination)
				.left(left)
				.middle(middle)
				.right(right)
				.build();
	}

}
