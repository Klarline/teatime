import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { Camera, MapPin, Coffee, X } from 'lucide-react';
import { blogApi } from '@/api/blog.api';
import { shopApi } from '@/api/shop.api';
import { uploadApi } from '@/api/upload.api';
import { stringifyImages } from '@/utils/formatters';
import type { Shop } from '@/api/types';

export const CreatePost = () => {
	const navigate = useNavigate();

	const [title, setTitle] = useState('');
	const [content, setContent] = useState('');
	const [images, setImages] = useState<string[]>([]);
	const [shopId, setShopId] = useState<number | undefined>(undefined);
	const [selectedShop, setSelectedShop] = useState<Shop | null>(null);
	const [uploading, setUploading] = useState(false);
	const [posting, setPosting] = useState(false);

	// Shop modal state
	const [showShopModal, setShowShopModal] = useState(false);
	const [shops, setShops] = useState<Shop[]>([]);
	const [loadingShops, setLoadingShops] = useState(false);

	const handleImageSelect = async (e: React.ChangeEvent<HTMLInputElement>) => {
		const files = Array.from(e.target.files || []);

		if (files.length + images.length > 9) {
			toast.error('Maximum 9 images allowed');
			return;
		}

		// Validate file types and sizes
		for (const file of files) {
			if (!file.type.startsWith('image/')) {
				toast.error('Please upload image files only');
				return;
			}
			if (file.size > 5 * 1024 * 1024) {
				toast.error('Image size must be less than 5MB');
				return;
			}
		}

		setUploading(true);

		try {
			const uploadPromises = files.map(file => uploadApi.uploadBlogImage(file));
			const results = await Promise.all(uploadPromises);
			const urls = results.map(r => r.data);

			setImages([...images, ...urls]);
			toast.success(`${files.length} image${files.length > 1 ? 's' : ''} uploaded!`);
		} catch (err: any) {
			console.error('Upload failed:', err);
			toast.error(err.response?.data?.errorMsg || 'Failed to upload images');
		} finally {
			setUploading(false);
		}
	};

	const removeImage = (index: number) => {
		setImages(images.filter((_, i) => i !== index));
	};

	const handleShopModalOpen = async () => {
		setLoadingShops(true);
		setShowShopModal(true);

		try {
			// Fetch shops from first type (or could fetch all types)
			const response = await shopApi.getShopsByType(1, 1);
			if (response.success) {
				const records = response.data?.records || response.data || [];
				setShops(records);
			}
		} catch (err: any) {
			console.error('Failed to fetch shops:', err);
			toast.error('Failed to load shops');
		} finally {
			setLoadingShops(false);
		}
	};

	const handleSelectShop = (shop: Shop) => {
		setSelectedShop(shop);
		setShopId(shop.id);
		setShowShopModal(false);
		toast.success(`Tagged ${shop.name}`);
	};

	const handleRemoveShop = (e: React.MouseEvent) => {
		e.stopPropagation();
		setSelectedShop(null);
		setShopId(undefined);
	};

	const handleSubmit = async () => {
		// Validation
		if (!title.trim()) {
			toast.error('Please add a title');
			return;
		}

		if (!content.trim()) {
			toast.error('Please add some content');
			return;
		}

		if (images.length === 0) {
			toast.error('Please add at least one image');
			return;
		}

		setPosting(true);

		try {
			const response = await blogApi.createBlog({
				title: title.trim(),
				content: content.trim(),
				images: stringifyImages(images),
				shopId,
			});

			if (response.success) {
				toast.success('Post created successfully!');
				// Navigate to the new blog post
				navigate(`/blog/${response.data}`);
			} else {
				toast.error(response.errorMsg || 'Failed to create post');
			}
		} catch (err: any) {
			console.error('Failed to create post:', err);
			toast.error(err.response?.data?.errorMsg || 'Failed to create post');
		} finally {
			setPosting(false);
		}
	};

	const handleCancel = () => {
		if (title || content || images.length > 0) {
			toast((t) => (
				<div className="flex flex-col gap-2">
					<span className="font-medium">Discard this post?</span>
					<div className="flex gap-2 justify-end">
						<button
							onClick={() => {
								toast.dismiss(t.id);
								navigate(-1);
							}}
							className="px-3 py-1 bg-red-500 text-white rounded-lg text-sm font-medium"
						>
							Discard
						</button>
						<button
							onClick={() => toast.dismiss(t.id)}
							className="px-3 py-1 bg-gray-200 text-gray-700 rounded-lg text-sm font-medium"
						>
							Keep Editing
						</button>
					</div>
				</div>
			), {
				duration: 5000,
			});
		} else {
			navigate(-1);
		}
	};

	return (
		<div className="h-screen bg-white flex flex-col">
			{/* Header */}
			<div className="px-4 py-3 border-b border-gray-100 flex justify-between items-center">
				<button
					onClick={handleCancel}
					className="text-gray-500 hover:text-gray-800"
				>
					Cancel
				</button>
				<h2 className="font-bold text-lg">New Post</h2>
				<button
					onClick={handleSubmit}
					disabled={posting || uploading || !title.trim() || !content.trim() || images.length === 0}
					className="px-4 py-1.5 bg-primary-500 text-white rounded-full text-sm font-bold shadow-md hover:bg-primary-600 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
				>
					{posting ? 'Posting...' : 'Post'}
				</button>
			</div>

			{/* Content */}
			<div className="p-4 flex-1 overflow-y-auto">
				{/* Image Upload Section */}
				<div className="mb-4">
					<div className="flex gap-2 overflow-x-auto pb-2">
						{/* Upload Button */}
						<label className="w-24 h-24 bg-gray-50 rounded-lg flex flex-col items-center justify-center border-2 border-dashed border-gray-300 text-gray-400 shrink-0 hover:border-primary-500 hover:text-primary-500 transition-colors cursor-pointer">
							<input
								type="file"
								accept="image/*"
								multiple
								onChange={handleImageSelect}
								className="hidden"
								disabled={uploading || images.length >= 9}
							/>
							{uploading ? (
								<div className="animate-spin">⏳</div>
							) : (
								<>
									<Camera size={24} />
									<span className="text-xs mt-1">Add Photo</span>
								</>
							)}
						</label>

						{/* Image Previews */}
						{images.map((url, index) => (
							<div
								key={index}
								className="w-24 h-24 bg-gray-100 rounded-lg shrink-0 overflow-hidden relative"
							>
								<img
									src={url}
									alt={`Upload ${index + 1}`}
									className="w-full h-full object-cover"
								/>
								<button
									onClick={() => removeImage(index)}
									className="absolute top-1 right-1 bg-black/50 text-white rounded-full p-1 hover:bg-black/70"
								>
									<X size={12} />
								</button>
							</div>
						))}
					</div>
					<p className="text-xs text-gray-400 mt-2">
						{images.length}/9 images • {uploading ? 'Uploading...' : 'Tap to add photos'}
					</p>
				</div>

				{/* Title Input */}
				<input
					placeholder="Add a title..."
					value={title}
					onChange={(e) => setTitle(e.target.value)}
					maxLength={100}
					className="w-full text-xl font-bold mb-4 focus:outline-none placeholder:text-gray-300"
				/>

				{/* Content Textarea */}
				<textarea
					placeholder="Share your tea moment..."
					value={content}
					onChange={(e) => setContent(e.target.value)}
					maxLength={1000}
					className="w-full h-40 resize-none focus:outline-none text-gray-600 placeholder:text-gray-300 text-base"
				/>

				{/* Character Count */}
				<p className="text-xs text-gray-400 text-right mt-1">
					{content.length}/1000 characters
				</p>

				{/* Additional Options */}
				<div className="mt-4 border-t border-gray-100 pt-4 space-y-3">
					{/* Location - Still disabled */}
					<button
						className="flex items-center gap-3 w-full text-gray-400 py-2 cursor-not-allowed"
						disabled
					>
						<MapPin size={20} />
						<span className="flex-1 text-left text-sm">Add Location</span>
						<span className="text-xs text-gray-400">(Coming soon)</span>
					</button>

					{/* Tag Shop - Now functional */}
					<button
						onClick={handleShopModalOpen}
						className="flex items-center gap-3 w-full text-gray-600 py-2 hover:text-primary-500 transition-colors"
					>
						<Coffee size={20} className={selectedShop ? 'text-primary-500' : ''} />
						<span className={`flex-1 text-left text-sm ${selectedShop ? 'text-primary-500 font-medium' : ''}`}>
							{selectedShop ? selectedShop.name : 'Tag a Shop'}
						</span>
						{selectedShop && (
							<button
								onClick={handleRemoveShop}
								className="text-gray-400 hover:text-gray-600"
							>
								<X size={16} />
							</button>
						)}
					</button>
				</div>
			</div>

			{/* Shop Selection Modal */}
			{showShopModal && (
				<div className="fixed inset-0 bg-black/50 z-50 flex items-end">
					<div className="bg-white w-full rounded-t-2xl max-h-[70vh] overflow-hidden flex flex-col">
						{/* Modal Header */}
						<div className="p-4 border-b flex justify-between items-center sticky top-0 bg-white">
							<h3 className="font-bold text-lg">Select a Shop</h3>
							<button
								onClick={() => setShowShopModal(false)}
								className="p-1 hover:bg-gray-100 rounded-full"
							>
								<X size={20} />
							</button>
						</div>

						{/* Shop List */}
						<div className="p-4 space-y-2 overflow-y-auto">
							{loadingShops ? (
								<div className="flex justify-center py-8">
									<div className="animate-spin text-primary-500">⏳</div>
								</div>
							) : shops.length > 0 ? (
								shops.map(shop => (
									<button
										key={shop.id}
										onClick={() => handleSelectShop(shop)}
										className="w-full text-left p-3 border border-gray-200 rounded-lg hover:border-primary-500 hover:bg-gray-50 transition-colors flex items-center gap-3"
									>
										<Coffee size={20} className="text-gray-400" />
										<div className="flex-1">
											<div className="font-medium text-gray-900">{shop.name}</div>
											<div className="text-xs text-gray-500">{shop.area}</div>
										</div>
									</button>
								))
							) : (
								<div className="text-center py-8 text-gray-500">
									<p>No shops available</p>
								</div>
							)}
						</div>
					</div>
				</div>
			)}
		</div>
	);
};