//
//  DYAVCaptureVideoPreviewViewManager.m
//  QRCodeForNPM
//
//  Created by ff on 2019/6/28.
//  Copyright © 2019 Facebook. All rights reserved.
//

#import "DYAVCaptureVideoPreviewViewManager.h"
#import <React/RCTBridge.h>
#import <React/RCTEventDispatcher.h>
#import "DYQRCodeAbility.h"

@interface DYAVCaptureVideoPreviewViewManager ()

@property (nonatomic, strong)DYPreviewView *previewView;

@end

@implementation DYAVCaptureVideoPreviewViewManager

RCT_EXPORT_MODULE(QrCodeView)

RCT_EXPORT_VIEW_PROPERTY(scanImageName, NSString);
RCT_EXPORT_VIEW_PROPERTY(borderColor, UIColor);
RCT_EXPORT_VIEW_PROPERTY(cornerColor, UIColor);
RCT_EXPORT_VIEW_PROPERTY(cornerWidth, CGFloat);
RCT_EXPORT_VIEW_PROPERTY(backgroundAlpha, CGFloat);
RCT_EXPORT_VIEW_PROPERTY(animationTimeInterval, NSTimeInterval);


- (UIView *)view {
  if (!self.previewView) {
    self.previewView = [DYPreviewView new];
  }
  return self.previewView;
}

#pragma mark - 初始化扫描对象(UI使用,已开启扫描)
RCT_EXPORT_METHOD(initScanQRCodeWithCallback:(RCTResponseSenderBlock)callback){
  dispatch_async(dispatch_get_main_queue(), ^{
      DYQRCodeAbility *ability = [DYQRCodeAbility shareAbility];
      BOOL isAuth = [ability initScanQRCodeWithSuperView:self.view];
      if (isAuth == YES) {
          ability.abilityScanQRCodeBlock = ^(NSDictionary * _Nonnull result) {
              callback(@[result]);
          };
      }else {
          callback(@[@{@"code":@"201",@"msg":@"no Auth",@"resp":@""}]);
      }
      
  });
}

#pragma mark - 开启/关闭扫描
RCT_EXPORT_METHOD(scanMessionSwitch:(bool)open){
  dispatch_async(dispatch_get_main_queue(), ^{
    DYQRCodeAbility *ability = [DYQRCodeAbility shareAbility];
    [ability scanMessionSwitch:open];
    [self->_previewView handleAnimation:open];
  });
}

@end
