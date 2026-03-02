import { authApi } from '../auth.api';

const mockPost = jest.fn();
const mockGet = jest.fn();

jest.mock('../index', () => ({
  __esModule: true,
  default: {
    post: (...args: unknown[]) => mockPost(...args),
    get: (...args: unknown[]) => mockGet(...args),
  },
}));

describe('authApi', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('sendCode', () => {
    it('sends verification code with phone number', async () => {
      mockPost.mockResolvedValue({
        data: { success: true, data: null },
      });

      const result = await authApi.sendCode('6045551234');

      expect(mockPost).toHaveBeenCalledWith(
        '/user/code',
        expect.any(URLSearchParams),
        { headers: { 'Content-Type': 'application/x-www-form-urlencoded' } }
      );
      expect(result.success).toBe(true);
    });
  });

  describe('login', () => {
    it('logs in with phone and code', async () => {
      mockPost.mockResolvedValue({
        data: { success: true, data: 'token-123' },
      });

      const result = await authApi.login('6045551234', '123456');

      expect(mockPost).toHaveBeenCalledWith('/user/login', {
        phone: '6045551234',
        code: '123456',
      });
      expect(result.success).toBe(true);
      expect(result.data).toBe('token-123');
    });
  });

  describe('logout', () => {
    it('calls logout endpoint', async () => {
      mockPost.mockResolvedValue({
        data: { success: true, data: null },
      });

      await authApi.logout();

      expect(mockPost).toHaveBeenCalledWith('/user/logout');
    });
  });

  describe('getCurrentUser', () => {
    it('fetches current user', async () => {
      const mockUser = { id: 1, nickName: 'TestUser', icon: '' };
      mockGet.mockResolvedValue({
        data: { success: true, data: mockUser },
      });

      const result = await authApi.getCurrentUser();

      expect(mockGet).toHaveBeenCalledWith('/user/me');
      expect(result.data).toEqual(mockUser);
    });
  });

  describe('checkIn', () => {
    it('performs daily check-in', async () => {
      mockPost.mockResolvedValue({
        data: { success: true, data: 5 },
      });

      const result = await authApi.checkIn();

      expect(mockPost).toHaveBeenCalledWith('/user/checkin');
      expect(result.data).toBe(5);
    });
  });
});
