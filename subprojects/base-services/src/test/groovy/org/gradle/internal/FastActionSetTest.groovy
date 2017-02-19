/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.internal

import org.gradle.api.Action
import spock.lang.Specification

class FastActionSetTest extends Specification {
    def "creates an empty set that does nothing"() {
        expect:
        def set = FastActionSet.empty()
        set.execute("value")
        set.empty
    }

    def "can add no-op action to empty set"() {
        expect:
        def set = FastActionSet.empty().add(Actions.doNothing())
        set.is(FastActionSet.empty())
    }

    def "can add empty set to empty set"() {
        expect:
        def set = FastActionSet.empty().add(FastActionSet.empty())
        set.is(FastActionSet.empty())
    }

    def "can create singleton set"() {
        def action = Mock(Action)

        when:
        def set = FastActionSet.empty().add(action)

        then:
        !set.empty

        when:
        set.execute("value")

        then:
        1 * action.execute("value")
        0 * _
    }

    def "can add no-op action to singleton set"() {
        def action = Mock(Action)

        expect:
        def set = FastActionSet.empty().add(action)
        set.add(Actions.doNothing()).is(set)
    }

    def "can add empty set to singleton set"() {
        def action = Mock(Action)

        expect:
        def set = FastActionSet.empty().add(action)
        set.add(FastActionSet.empty()).is(set)
    }

    def "can add duplicate action to singleton set"() {
        def action = Mock(Action)

        expect:
        def set = FastActionSet.empty().add(action)
        set.add(action).is(set)
        set.add(FastActionSet.of(action)).is(set)
    }

    def "can add action to singleton set"() {
        def action1 = Mock(Action)
        def action2 = Mock(Action)

        when:
        def set = FastActionSet.empty().add(action1).add(action2)

        then:
        !set.empty

        when:
        set.execute("value")

        then:
        1 * action1.execute("value")
        1 * action2.execute("value")
        0 * _
    }

    def "can create a set from multiple actions"() {
        def action1 = Mock(Action)
        def action2 = Mock(Action)

        when:
        def set1 = FastActionSet.of(action1, action2)

        then:
        !set1.empty

        when:
        set1.execute("value")

        then:
        1 * action1.execute("value")
        1 * action2.execute("value")
        0 * _

        when:
        def set2 = FastActionSet.of(action1)

        then:
        !set2.empty

        when:
        set2.execute("value")

        then:
        1 * action1.execute("value")
        0 * _

        when:
        def set3 = FastActionSet.of()

        then:
        set3.is(FastActionSet.empty())
    }

    def "can add no-op action to composite set"() {
        def action1 = Mock(Action)
        def action2 = Mock(Action)

        expect:
        def set = FastActionSet.of(action1, action2)
        set.add(Actions.doNothing()).is(set)
    }

    def "can add empty set to composite set"() {
        def action1 = Mock(Action)
        def action2 = Mock(Action)

        expect:
        def set = FastActionSet.of(action1, action2)
        set.add(FastActionSet.empty()).is(set)
    }

    def "can add duplicate action to composite set"() {
        def action1 = Mock(Action)
        def action2 = Mock(Action)

        expect:
        def set = FastActionSet.of(action1, action2)
        set.add(action1).is(set)
        set.add(action2).is(set)
        set.add(FastActionSet.of(action1)).is(set)
        set.add(FastActionSet.of(action2)).is(set)
        set.add(FastActionSet.of(action1, action2)).is(set)
        set.add(FastActionSet.of(action2, action1)).is(set)
        FastActionSet.of(action1).add(set).is(set)
    }

    def "can add self to composite set"() {
        def action1 = Mock(Action)
        def action2 = Mock(Action)

        expect:
        def set = FastActionSet.of(action1, action2)
        set.add(set).is(set)
    }

    def "can add action to composite set"() {
        def action1 = Mock(Action)
        def action2 = Mock(Action)
        def action3 = Mock(Action)

        when:
        def set = FastActionSet.of(action1, action2).add(action3)

        then:
        !set.empty

        when:
        set.execute("value")

        then:
        1 * action1.execute("value")
        1 * action2.execute("value")
        1 * action3.execute("value")
        0 * _
    }

    def "deduplicates actions"() {
        def action1 = Mock(Action)
        def action2 = Mock(Action)
        def action3 = Mock(Action)

        when:
        def set = FastActionSet.of(action1, action2, FastActionSet.of(action3, action1)).add(FastActionSet.of(action2)).add(action3)

        then:
        !set.empty

        when:
        set.execute("value")

        then:
        1 * action1.execute("value")
        1 * action2.execute("value")
        1 * action3.execute("value")
        0 * _
    }

    def "ignores no-op actions when creating a composite"() {
        expect:
        FastActionSet.of(Actions.doNothing(), Actions.doNothing()).is(FastActionSet.empty())
    }

    def "ignores empty sets is a composite"() {
        expect:
        FastActionSet.of(FastActionSet.empty(), FastActionSet.empty()).is(FastActionSet.empty())
    }
}