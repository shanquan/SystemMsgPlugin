//
//  SystemMsg.m
//
//  Created by wangweili on 2018/2/27.
//  Copyright © 2018年 wangweili. All rights reserved.
//

#import <Cordova/CDVPlugin.h>

@interface SystemMsg : CDVPlugin

- (void)listenCall:(CDVInvokedUrlCommand*)command;
- (void)cancelListenCall:(CDVInvokedUrlCommand*)command;
@end
