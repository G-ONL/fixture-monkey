package com.navercorp.fixturemonkey.generator;

import static com.navercorp.fixturemonkey.Constants.NO_OR_ALL_INDEX_INTEGER_VALUE;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import net.jqwik.api.Arbitrary;

import com.navercorp.fixturemonkey.arbitrary.ArbitraryNode;
import com.navercorp.fixturemonkey.arbitrary.ArbitraryType;
import com.navercorp.fixturemonkey.customizer.WithFixtureCustomizer;

public abstract class AbstractArbitraryGenerator implements ArbitraryGenerator, WithFixtureCustomizer {
	@SuppressWarnings({"unchecked", "rawtypes"})
	protected <T> Arbitrary<T> generateContainer(ArbitraryType type, List<ArbitraryNode> nodes) {
		boolean indexNotValid = nodes.stream().anyMatch(it -> it.getIndexOfIterable() == NO_OR_ALL_INDEX_INTEGER_VALUE);

		if (indexNotValid) {
			throw new IllegalArgumentException("Container index should not be all index or no index.");
		}

		if (type.isArray()) {
			ArbitraryType<?> arrayType = type.getArrayFixtureType();
			Class<?> arrayClazz = arrayType.getType();
			return (Arbitrary<T>)ArrayBuilder.INSTANCE.build(arrayClazz, nodes);
		}

		return (Arbitrary<T>)CollectionBuilders.build(type.getType(), nodes);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public final <T> Arbitrary<T> generate(ArbitraryType type, List<ArbitraryNode> nodes) {
		if (type.isContainer()) {
			return this.generateContainer(type, nodes);
		}

		return generateObject(type, nodes);
	}

	@SuppressWarnings("rawtypes")
	protected abstract <T> Arbitrary<T> generateObject(ArbitraryType type, List<ArbitraryNode> nodes);

	@SuppressWarnings("rawtypes")
	protected Map<String, Arbitrary> toArbitrariesByFieldName(
		List<ArbitraryNode> nodes,
		Function<ArbitraryNode, String> resolveFieldName,
		BiFunction<ArbitraryNode, Arbitrary, Arbitrary> resolveArbitrary
	) {
		Map<String, Arbitrary> map = new HashMap<>();
		for (ArbitraryNode node : nodes) {
			map.put(resolveFieldName.apply(node), resolveArbitrary.apply(node, node.getArbitrary()));
		}
		return Collections.unmodifiableMap(map);
	}
}
