package com.araguacaima.commons.utils.properties.strategy;

public interface PropertiesHandlerFactory {

    PropertiesHandlerStrategyInterface getStrategy(String strategyName);
}