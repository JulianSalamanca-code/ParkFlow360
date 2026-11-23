const mockPaths = ['/api/empresa/', '/api/cajero/', '/api/guardia/'];
module.exports = {
  '/api': {
    target: 'http://localhost:8080',
    secure: false,
    changeOrigin: true,
    bypass: function (req) {
      if (mockPaths.some(p => req.url.startsWith(p))) {
        return req.url;
      }
      return null;
    }
  }
};
