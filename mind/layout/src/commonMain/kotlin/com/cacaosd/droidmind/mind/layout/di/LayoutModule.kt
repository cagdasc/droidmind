package com.cacaosd.droidmind.mind.layout.di

import com.cacaosd.droidmind.core.config.di.SelfResolveQualifier
import com.cacaosd.droidmind.core.config.di.coreConfigModule
import com.cacaosd.droidmind.mind.layout.optimisation_strategy.CollapseParentNodeStrategy
import com.cacaosd.droidmind.mind.layout.optimisation_strategy.NodeOptimisationStrategy
import com.cacaosd.droidmind.mind.layout.parser.AndroidLayoutParser
import com.cacaosd.droidmind.mind.layout.parser.IosLayoutParser
import com.cacaosd.droidmind.mind.layout.parser.LayoutParser
import org.koin.dsl.bind
import org.koin.dsl.module

object AndroidLayoutParserQualifier : SelfResolveQualifier()
object AndroidLayoutNodeOptimisationStrategyQualifier : SelfResolveQualifier()

object IosLayoutParserQualifier : SelfResolveQualifier()
object IosLayoutNodeOptimisationStrategyQualifier : SelfResolveQualifier()

val layoutModule = module {
    includes(coreConfigModule)

    single(AndroidLayoutNodeOptimisationStrategyQualifier) { CollapseParentNodeStrategy() } bind NodeOptimisationStrategy::class
    single(AndroidLayoutParserQualifier) {
        AndroidLayoutParser(
            xml = get(),
            uiAutomatorNodeOptimisationStrategy = get(AndroidLayoutNodeOptimisationStrategyQualifier)
        )
    } bind LayoutParser::class

    single(IosLayoutNodeOptimisationStrategyQualifier) { CollapseParentNodeStrategy() } bind NodeOptimisationStrategy::class
    single(IosLayoutParserQualifier) {
        IosLayoutParser(
            xml = get(),
            nodeOptimisationStrategy = get(IosLayoutNodeOptimisationStrategyQualifier)
        )
    } bind LayoutParser::class

}