// src/pages/Profile.tsx
import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { Share2, LogOut, Settings, ChevronLeft } from 'lucide-react';
import { useAuthStore } from '@/store/authStore';
import { useUserStore } from '@/store/userStore';
import { blogApi } from '@/api/blog.api';
import { authApi } from '@/api/auth.api';
import { followApi } from '@/api/follow.api';
import { parseImages } from '@/utils/formatters';
import type { Blog, User } from '@/api/types';

export const Profile = () => {
	const { id } = useParams<{ id: string }>();
	const navigate = useNavigate();

	const currentUser = useAuthStore((state) => state.user);
	const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
	const logout = useAuthStore((state) => state.logout);
	const checkinStreak = useUserStore((state) => state.checkinStreak);
	const setCheckinStreak = useUserStore((state) => state.setCheckinStreak);

	const [profileUser, setProfileUser] = useState<User | null>(null);
	const [blogs, setBlogs] = useState<Blog[]>([]);
	const [loading, setLoading] = useState(true);
	const [isFollowing, setIsFollowing] = useState(false);
	const [followLoading, setFollowLoading] = useState(false);

	// Stats
	const [stats, setStats] = useState({
		following: 0,
		followers: 0,
		posts: 0,
	});

	// Determine if viewing own profile
	const isOwnProfile = !id || (currentUser && id === currentUser.id.toString());

	// Helper function to fetch own profile data
	const fetchOwnProfileData = async (user: User) => {
		setProfileUser(user);

		// Fetch own blogs
		const blogsResponse = await blogApi.getMyBlogs(1);
		if (blogsResponse.success) {
			const records = blogsResponse.data?.records || blogsResponse.data || [];
			setBlogs(records);
			setStats(prev => ({
				...prev,
				posts: blogsResponse.data?.total || records.length
			}));
		}

		// Fetch check-in streak
		const streakResponse = await authApi.getCheckInCount();
		if (streakResponse.success) {
			setCheckinStreak(streakResponse.data);
		}

		// Fetch follower/following counts
		await fetchFollowCounts(user.id);
	};

	// Helper function to fetch other user's profile data
	const fetchOtherUserProfileData = async (userId: number) => {
		// Fetch user info
		const userResponse = await authApi.getUserInfo(userId);
		if (userResponse.success) {
			setProfileUser(userResponse.data);
		}

		// Fetch user's blogs
		const blogsResponse = await blogApi.getBlogsByUserId(userId, 1);
		if (blogsResponse.success) {
			const records = blogsResponse.data?.records || blogsResponse.data || [];
			setBlogs(records);
			setStats(prev => ({
				...prev,
				posts: blogsResponse.data?.total || records.length
			}));
		}

		// Check if following
		const followResponse = await followApi.isFollowing(userId);
		if (followResponse.success) {
			setIsFollowing(followResponse.data);
		}

		// Fetch follower/following counts
		await fetchFollowCounts(userId);
	};

	// Helper function to fetch follow counts
	const fetchFollowCounts = async (userId: number) => {
		try {
			const [followersRes, followingRes] = await Promise.all([
				followApi.getFollowerCount(userId),
				followApi.getFollowingCount(userId),
			]);

			if (followersRes.success && followingRes.success) {
				setStats(prev => ({
					...prev,
					followers: followersRes.data || 0,
					following: followingRes.data || 0,
				}));
			}
		} catch (error) {
			console.error('Failed to fetch follow counts:', error);
		}
	};

	useEffect(() => {
		const fetchProfileData = async () => {
			try {
				setLoading(true);

				if (isOwnProfile) {
					// Viewing own profile
					if (!currentUser) {
						// User is authenticated but data not loaded yet
						if (isAuthenticated) {
							// Try to fetch current user first
							const userResponse = await authApi.getCurrentUser();
							if (userResponse.success) {
								await fetchOwnProfileData(userResponse.data);
							} else {
								// Failed to fetch user, redirect to login
								navigate('/login', { replace: true });
								return;
							}
						} else {
							// Not authenticated, redirect to login
							navigate('/login', { replace: true });
							return;
						}
					} else {
						// currentUser exists, proceed normally
						await fetchOwnProfileData(currentUser);
					}
				} else {
					// Viewing another user's profile
					await fetchOtherUserProfileData(parseInt(id!));
				}
			} catch (error) {
				console.error('Failed to fetch profile data:', error);
			} finally {
				setLoading(false);
			}
		};

		fetchProfileData();
	}, [id, isOwnProfile, currentUser, isAuthenticated, navigate, setCheckinStreak]);

	const handleLogout = () => {
		toast((t) => (
			<div className="flex flex-col gap-2">
				<span className="font-medium">Are you sure you want to logout?</span>
				<div className="flex gap-2 justify-end">
					<button
						onClick={() => {
							toast.dismiss(t.id);
							logout();
							navigate('/login');
							toast.success('Logged out successfully');
						}}
						className="px-3 py-1 bg-red-500 text-white rounded-lg text-sm"
					>
						Logout
					</button>
					<button
						onClick={() => toast.dismiss(t.id)}
						className="px-3 py-1 bg-gray-200 text-gray-700 rounded-lg text-sm"
					>
						Cancel
					</button>
				</div>
			</div>
		), {
			duration: 5000,
		});
	};

	const handleFollow = async () => {
		if (!profileUser || isOwnProfile) return;

		setFollowLoading(true);
		try {
			await followApi.toggleFollow(profileUser.id, !isFollowing);
			setIsFollowing(!isFollowing);

			// Update follower count
			setStats(prev => ({
				...prev,
				followers: isFollowing ? prev.followers - 1 : prev.followers + 1
			}));
		} catch (error) {
			console.error('Failed to toggle follow:', error);
		} finally {
			setFollowLoading(false);
		}
	};

	const handleBlogClick = (blog: Blog) => {
		navigate(`/blog/${blog.id}`);
	};

	const handleShare = () => {
		if (navigator.share && profileUser) {
			navigator.share({
				title: `${profileUser.nickName}'s Profile`,
				text: `Check out ${profileUser.nickName} on TeaTime!`,
				url: window.location.href,
			});
		}
	};

	if (loading) {
		return (
			<div className="min-h-screen flex items-center justify-center">
				<div className="animate-spin text-primary-500">⏳</div>
			</div>
		);
	}

	if (!profileUser) {
		return (
			<div className="min-h-screen flex flex-col items-center justify-center p-8">
				<p className="text-gray-500 mb-4">User not found</p>
				<button
					onClick={() => navigate(-1)}
					className="text-primary-500 underline"
				>
					Go Back
				</button>
			</div>
		);
	}

	return (
		<>
			{/* Header Image with Actions */}
			<div className="h-40 bg-secondary-500 relative">
				{!isOwnProfile && (
					<div className="absolute top-4 left-4">
						<button
							onClick={() => navigate(-1)}
							className="p-2 bg-black/20 rounded-full text-white backdrop-blur-sm hover:bg-black/30"
						>
							<ChevronLeft size={18} />
						</button>
					</div>
				)}
				<div className="absolute top-4 right-4 flex gap-3">
					<button
						onClick={handleShare}
						className="p-2 bg-black/20 rounded-full text-white backdrop-blur-sm hover:bg-black/30"
					>
						<Share2 size={18} />
					</button>
					{isOwnProfile && (
						<button
							onClick={handleLogout}
							className="p-2 bg-black/20 rounded-full text-white backdrop-blur-sm hover:bg-black/30"
						>
							<LogOut size={18} />
						</button>
					)}
				</div>
			</div>

			{/* Profile Info */}
			<div className="px-4 -mt-12 mb-6">
				<div className="flex justify-between items-end mb-4">
					<div className="relative">
						<img
							src={profileUser.icon || 'https://api.dicebear.com/7.x/thumbs/svg?seed=default&colors=22c55e'}
							alt={profileUser.nickName}
							className="w-24 h-24 rounded-full border-4 border-white bg-white"
						/>
						<div className="absolute bottom-1 right-1 bg-primary-500 w-6 h-6 rounded-full flex items-center justify-center border-2 border-white">
							<span className="text-white text-xs">✓</span>
						</div>
					</div>

					{isOwnProfile ? (
						<button
							onClick={() => navigate('/settings')}
							className="px-6 py-2 border border-gray-300 rounded-full font-medium text-sm hover:bg-gray-50 text-gray-700 flex items-center gap-2"
						>
							<Settings size={16} />
							Edit Profile
						</button>
					) : (
						<button
							onClick={handleFollow}
							disabled={followLoading}
							className={`px-6 py-2 rounded-full font-medium text-sm transition-colors ${isFollowing
								? 'border border-gray-300 text-gray-700 hover:bg-gray-50'
								: 'bg-primary-500 text-white hover:bg-primary-600'
								}`}
						>
							{followLoading ? 'Loading...' : isFollowing ? 'Following' : 'Follow'}
						</button>
					)}
				</div>

				<h2 className="text-2xl font-bold text-gray-900 truncate max-w-full">
					{profileUser.nickName}
				</h2>

				<p className="text-gray-500 text-sm mb-4 truncate">
					{isOwnProfile ? 'Tea enthusiast 🍵 | Exploring flavors 🌍' : `Joined ${new Date(profileUser.createTime).getFullYear()}`}
				</p>

				{/* Stats */}
				<div className="flex gap-8">
					<div>
						<span className="block font-bold text-lg text-gray-900">{stats.following}</span>
						<span className="text-xs text-gray-500">Following</span>
					</div>
					<div>
						<span className="block font-bold text-lg text-gray-900">{stats.followers}</span>
						<span className="text-xs text-gray-500">Followers</span>
					</div>
					<div>
						<span className="block font-bold text-lg text-gray-900">{stats.posts}</span>
						<span className="text-xs text-gray-500">Posts</span>
					</div>
					{isOwnProfile && (
						<div className="pl-4 border-l border-gray-200">
							<span className="block font-bold text-lg text-primary-500">🔥 {checkinStreak}</span>
							<span className="text-xs text-gray-500">Day Streak</span>
						</div>
					)}
				</div>
			</div>

			{/* Tabs */}
			<div className="border-t border-b border-gray-100 flex">
				<button className="flex-1 py-3 text-sm font-medium border-b-2 border-primary-500 text-primary-500">
					Posts
				</button>
				<button className="flex-1 py-3 text-sm font-medium text-gray-400 hover:text-gray-600">
					Likes
				</button>
				{isOwnProfile && (
					<button className="flex-1 py-3 text-sm font-medium text-gray-400 hover:text-gray-600">
						Saved
					</button>
				)}
			</div>

			{/* Posts Grid */}
			{blogs.length > 0 ? (
				<div className="grid grid-cols-3 gap-0.5 mt-0.5 pb-4">
					{blogs.map(blog => {
						const images = parseImages(blog.images);
						const firstImage = images[0] || 'https://images.unsplash.com/photo-1564890369478-c89ca6d9cde9?auto=format&fit=crop&q=80&w=400';
						return (
							<div
								key={blog.id}
								onClick={() => handleBlogClick(blog)}
								className="aspect-square bg-gray-100 relative group cursor-pointer"
							>
								<img
									src={firstImage}
									alt={blog.title}
									className="w-full h-full object-cover"
								/>
								{/* Hover overlay with stats */}
								<div className="absolute inset-0 bg-black/30 hidden group-hover:flex items-center justify-center gap-4 text-white font-bold">
									<div className="flex items-center gap-1">
										<span>❤️</span>
										<span>{blog.liked}</span>
									</div>
									<div className="flex items-center gap-1">
										<span>💬</span>
										<span>{blog.comments}</span>
									</div>
								</div>
							</div>
						);
					})}
				</div>
			) : (
				<div className="text-center py-16 text-gray-500">
					<p>{isOwnProfile ? 'No posts yet' : 'This user has no posts'}</p>
					{isOwnProfile && (
						<button
							onClick={() => navigate('/create')}
							className="mt-4 text-primary-500 underline"
						>
							Create your first post
						</button>
					)}
				</div>
			)}
		</>
	);
};