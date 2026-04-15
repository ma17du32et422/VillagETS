/** DO NOT TOUCH */

export const getBaseUrl = () => {
  if (window.location.hostname === 'localhost') {
    return 'http://localhost:5000';
  }
  return 'https://apivillagets.lesageserveur.com';
};

export const getBaseUrlWebsocket = () => {
    if (window.location.hostname === 'localhost') {
    return 'ws://localhost:5000';
  }
  return 'ws://apivillagets.lesageserveur.com';
};
