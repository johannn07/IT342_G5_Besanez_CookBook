import { multipartClient } from './APIClientFactory';

export const uploadImage = (file, folder = 'recipes') => {
    const formData = new FormData();
    formData.append('file', file);
    return multipartClient.post(`/api/image/upload?folder=${encodeURIComponent(folder)}`, formData);
};

const imageAPI = { uploadImage };
export default imageAPI;