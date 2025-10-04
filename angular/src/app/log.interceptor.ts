import { HttpInterceptorFn } from '@angular/common/http';

export const logInterceptor: HttpInterceptorFn = (req, next) => {
  console.log('[HTTP →]', req.method, '-', req.urlWithParams);
  return next(req);
};
