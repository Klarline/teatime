import React, { useState } from 'react';
import { Sparkles, Loader2 } from 'lucide-react';
import ReactMarkdown from 'react-markdown';
import { aiApi } from '@/api/ai';
import toast from 'react-hot-toast';

export const AIRecommendations: React.FC = () => {
	const [query, setQuery] = useState('');
	const [recommendations, setRecommendations] = useState('');
	const [loading, setLoading] = useState(false);

	const handleGetRecommendations = async () => {
		if (!query.trim()) {
			toast.error('Please enter what you\'re looking for');
			return;
		}

		setLoading(true);
		try {
			const response = await aiApi.getRecommendations({
				query,
				maxResults: 5
			});
			setRecommendations(response.recommendations);
		} catch (error) {
			toast.error('Failed to get recommendations');
			console.error(error);
		} finally {
			setLoading(false);
		}
	};

	return (
		<div className="bg-white rounded-xl shadow-sm border border-gray-100 p-4 md:p-6">
			{/* Header */}
			<div className="flex items-center gap-2 mb-4">
				<div className="w-8 h-8 bg-[#0f9289]/10 rounded-lg flex items-center justify-center">
					<Sparkles className="text-[#0f9289]" size={18} />
				</div>
				<div>
					<h2 className="text-lg font-bold text-gray-900">AI Recommendations</h2>
					<p className="text-xs text-gray-500">Powered by Gemini</p>
				</div>
			</div>

			{/* Input */}
			<div className="space-y-3">
				<input
					type="text"
					value={query}
					onChange={(e) => setQuery(e.target.value)}
					placeholder="e.g., 'Find me a quiet matcha cafe for studying'"
					className="w-full px-4 py-3 rounded-xl border border-gray-200 focus:outline-none focus:ring-2 focus:ring-[#0f9289]/30 focus:border-[#0f9289] transition-all bg-gray-50 text-sm"
					onKeyPress={(e) => e.key === 'Enter' && handleGetRecommendations()}
				/>

				<button
					onClick={handleGetRecommendations}
					disabled={loading}
					className="w-full px-4 py-2 rounded-lg bg-[#3da271] hover:bg-[#1f5d41] text-white font-medium transition-all active:scale-95 disabled:opacity-50 disabled:scale-100 shadow-md hover:shadow-lg flex items-center justify-center"
				>
					{loading ? (
						<>
							<Loader2 className="animate-spin mr-2" size={18} />
							Generating...
						</>
					) : (
						<>
							<Sparkles className="mr-2" size={18} />
							Get Recommendations
						</>
					)}
				</button>
			</div>

			{/* Results with Markdown */}
			{recommendations && (
				<div className="mt-6 p-4 bg-[#0f9289]/5 rounded-xl border border-[#0f9289]/20">
					<ReactMarkdown
						components={{
							p: ({ node, ...props }) => <p className="text-sm text-gray-700 leading-relaxed my-2" {...props} />,
							h1: ({ node, ...props }) => <h1 className="text-lg font-bold text-gray-900 mt-4 mb-2" {...props} />,
							h2: ({ node, ...props }) => <h2 className="text-base font-bold text-gray-900 mt-3 mb-2" {...props} />,
							h3: ({ node, ...props }) => <h3 className="text-sm font-bold text-gray-900 mt-2 mb-1" {...props} />,
							ul: ({ node, ...props }) => <ul className="list-disc list-inside my-2 text-sm text-gray-700 space-y-2" {...props} />,
							ol: ({ node, ...props }) => <ol className="list-decimal list-outside ml-5 my-2 text-sm text-gray-700 space-y-3" {...props} />,
							li: ({ node, ...props }) => <li className="text-gray-700 leading-relaxed" {...props} />,
							strong: ({ node, ...props }) => <strong className="font-semibold text-gray-900" {...props} />,
						}}
					>
						{recommendations}
					</ReactMarkdown>
				</div>
			)}
		</div>
	);
};