//
//  SystemMsg.m
//
//  Created by wangweili on 2018/2/27.
//  Copyright © 2018年 wangweili. All rights reserved.
//

#import "SystemMsg.h"
#import <CoreTelephony/CTCall.h>
#import <CoreTelephony/CTCallCenter.h>

@interface SystemMsg()
@property(nonatomic,strong)CTCallCenter*callCenter;
@end


@implementation SystemMsg

-(void)pluginInitialize{
    [super pluginInitialize];
    if (!_callCenter) {
         _callCenter = [[CTCallCenter alloc]init];
    }
}

- (void)listenCall:(CDVInvokedUrlCommand*)command{
    __block NSString*string=@"";
    __block SystemMsg *blockSelf = self;
    _callCenter.callEventHandler = ^(CTCall *call){       
        NSLog(@"call.callState :%@",call.callState);
        
        if ([call.callState isEqualToString:@"CTCallStateIncoming"]) {
            
            string=@"oncall";//正在呼叫状态
        }
        else if ([call.callState isEqualToString:@"CTCallStateConnected"]) {
            
            string=@"offcall";
        }
        else if ([call.callState isEqualToString:@"CTCallStateDisconnected"]) {
            if([string isEqualToString:@"offcall"]){
                //接通后再挂断不发消息；
                return;
            }else{
                //直接挂断
                string=@"offcall";
            }
        }else{
            string=@"";
        }
        if(![string isEqualToString:@""]){
            NSDictionary* msg = @{
                                  @"type": string
                                  };
            CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:msg];
            [pluginResult setKeepCallbackAsBool:true];
            [blockSelf.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        }
    }; 
    
    
}
- (void)cancelListenCall:(CDVInvokedUrlCommand*)command{
    _callCenter.callEventHandler = nil;
    //always return OK
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}
@end
