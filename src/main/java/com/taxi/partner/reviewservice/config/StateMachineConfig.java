package com.taxi.partner.reviewservice.config;

import com.taxi.partner.reviewservice.domain.Review;
import com.taxi.partner.reviewservice.domain.ReviewEventEnum;
import com.taxi.partner.reviewservice.domain.ReviewStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import java.util.EnumSet;

@Slf4j
@EnableStateMachineFactory
@Configuration
public class StateMachineConfig  extends StateMachineConfigurerAdapter<ReviewStatusEnum, ReviewEventEnum> {

    @Override
    public void configure(StateMachineStateConfigurer<ReviewStatusEnum, ReviewEventEnum> states) throws Exception{
        states.withStates()
                .initial(ReviewStatusEnum.NEW)
                .states(EnumSet.allOf(ReviewStatusEnum.class))
                .end(ReviewStatusEnum.CANCELLED)
                //.end(ReviewStatusEnum.PICKED_UP)
                .end(ReviewStatusEnum.SHIPPED);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<ReviewStatusEnum, ReviewEventEnum> transitions) throws Exception {
        //transitions.withExternal().source(ReviewStatusEnum.NEW);

    }
}
